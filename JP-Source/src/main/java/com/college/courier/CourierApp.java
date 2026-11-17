package com.college.courier;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class CourierApp {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== COURIER AND PARCEL TRACKING MANAGEMENT SYSTEM ===");
        try (Connection ignored = DB.getConnection()) {
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check MySQL service, schema scripts and DB_URL/DB_USER/DB_PASSWORD.");
            return;
        }

        while (true) {
            System.out.println("\\n1 Add customer  2 List customers  3 Register parcel  4 Assign personnel");
            System.out.println("5 Update parcel status  6 Record delivery attempt  7 Track parcel");
            System.out.println("8 Shipment report  0 Exit");
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1 -> addCustomer();
                    case 2 -> listCustomers();
                    case 3 -> registerParcel();
                    case 4 -> assignPersonnel();
                    case 5 -> updateStatus();
                    case 6 -> recordAttempt();
                    case 7 -> trackParcel();
                    case 8 -> report();
                    case 0 -> { System.out.println("Goodbye."); return; }
                    default -> System.out.println("Invalid menu option.");
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Input error: " + e.getMessage());
            }
        }
    }

    private static String read(String label) {
        System.out.print(label);
        return in.nextLine().trim();
    }
    private static int readInt(String label) {
        while (true) {
            try { return Integer.parseInt(read(label)); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }
    private static BigDecimal readDecimal(String label) {
        while (true) {
            try {
                BigDecimal n = new BigDecimal(read(label));
                if (n.signum() <= 0) throw new NumberFormatException();
                return n;
            } catch (NumberFormatException e) { System.out.println("Enter a number greater than zero."); }
        }
    }
    private static void addCustomer() throws SQLException {
        String name = required("Customer name: ");
        String phone = required("Phone: ");
        String email = read("Email (optional): ");
        String address = required("Address: ");
        String city = required("City: ");
        String sql = "INSERT INTO customers(full_name,phone,email,address_line,city) VALUES(?,?,?,?,?)";
        try (Connection c=DB.getConnection(); PreparedStatement ps=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,name); ps.setString(2,phone); ps.setString(3,email.isBlank()?null:email);
            ps.setString(4,address); ps.setString(5,city); ps.executeUpdate();
            try (ResultSet rs=ps.getGeneratedKeys()) { if(rs.next()) System.out.println("Customer created. ID: "+rs.getLong(1)); }
        }
    }
    private static void listCustomers() throws SQLException {
        try (Connection c=DB.getConnection(); Statement s=c.createStatement();
             ResultSet rs=s.executeQuery("SELECT customer_id,full_name,phone,city FROM customers ORDER BY customer_id")) {
            System.out.println("ID | Name | Phone | City");
            while(rs.next()) System.out.printf("%d | %s | %s | %s%n",rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4));
        }
    }
    private static void registerParcel() throws SQLException {
        long sender = readInt("Sender customer ID: ");
        String receiver = required("Receiver name: ");
        String phone = required("Receiver phone: ");
        String address = required("Delivery address: ");
        String city = required("Destination city: ");
        String zone = read("Destination zone (LOCAL/REGIONAL/NATIONAL): ").toUpperCase();
        if (!zone.matches("LOCAL|REGIONAL|NATIONAL")) throw new IllegalArgumentException("Zone must be LOCAL, REGIONAL or NATIONAL.");
        BigDecimal weight = readDecimal("Weight in kg: ");
        BigDecimal surcharge = switch(zone) { case "LOCAL" -> new BigDecimal("0"); case "REGIONAL" -> new BigDecimal("15"); default -> new BigDecimal("70"); };
        BigDecimal charge = new BigDecimal("50").add(weight.multiply(new BigDecimal("10"))).add(surcharge);
        String tracking = "CP" + LocalDate.now().toString().replace("-","") + UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase();
        String sql = "INSERT INTO parcels(tracking_number,sender_customer_id,receiver_name,receiver_phone,delivery_address,destination_city,destination_zone,weight_kg,delivery_charge,current_status,estimated_delivery_date) VALUES(?,?,?,?,?,?,?,?,?,'BOOKED',?)";
        try (Connection c=DB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,tracking); ps.setLong(2,sender); ps.setString(3,receiver); ps.setString(4,phone);
                ps.setString(5,address); ps.setString(6,city); ps.setString(7,zone); ps.setBigDecimal(8,weight);
                ps.setBigDecimal(9,charge); ps.setDate(10,Date.valueOf(LocalDate.now().plusDays(zone.equals("LOCAL")?1:zone.equals("REGIONAL")?3:5)));
                ps.executeUpdate();
                try(ResultSet keys=ps.getGeneratedKeys()) {
                    keys.next(); long parcelId=keys.getLong(1);
                    addHistory(c,parcelId,"BOOKED",city,"Parcel registered",null);
                }
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
        }
        System.out.println("Parcel registered. Tracking number: "+tracking+" | Charge: Rs. "+charge);
    }
    private static void assignPersonnel() throws SQLException {
        String tracking=required("Tracking number: ");
        long personnel=readInt("Personnel ID: ");
        try(Connection c=DB.getConnection(); PreparedStatement ps=c.prepareStatement(
            "UPDATE parcels SET assigned_personnel_id=? WHERE tracking_number=?")) {
            ps.setLong(1,personnel); ps.setString(2,tracking);
            if(ps.executeUpdate()==0) System.out.println("Tracking number not found."); else System.out.println("Personnel assigned.");
        }
    }
    private static void updateStatus() throws SQLException {
        String tracking=required("Tracking number: ");
        String status=read("New status (BOOKED/PICKED_UP/IN_TRANSIT/OUT_FOR_DELIVERY/DELIVERED/DELIVERY_FAILED/RETURNED): ").toUpperCase();
        if(!status.matches("BOOKED|PICKED_UP|IN_TRANSIT|OUT_FOR_DELIVERY|DELIVERED|DELIVERY_FAILED|RETURNED"))
            throw new IllegalArgumentException("Unsupported status.");
        String location=required("Current location: ");
        String remarks=read("Remarks: ");
        String personText=read("Updated by personnel ID (blank if office): ");
        Long person=personText.isBlank()?null:Long.parseLong(personText);
        try(Connection c=DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement ps=c.prepareStatement("UPDATE parcels SET current_status=?, delivered_at=? WHERE tracking_number=?")) {
                    ps.setString(1,status); ps.setTimestamp(2,status.equals("DELIVERED")?new Timestamp(System.currentTimeMillis()):null); ps.setString(3,tracking);
                    if(ps.executeUpdate()==0) throw new IllegalArgumentException("Tracking number not found.");
                }
                long id;
                try(PreparedStatement ps=c.prepareStatement("SELECT parcel_id FROM parcels WHERE tracking_number=?")) {
                    ps.setString(1,tracking); try(ResultSet rs=ps.executeQuery()){rs.next(); id=rs.getLong(1);}
                }
                addHistory(c,id,status,location,remarks,person);
                c.commit(); System.out.println("Status updated and tracking history saved.");
            } catch(Exception ex) { c.rollback(); if(ex instanceof SQLException se) throw se; if(ex instanceof RuntimeException re) throw re; throw new SQLException(ex); }
        }
    }
    private static void recordAttempt() throws SQLException {
        String tracking=required("Tracking number: ");
        String outcome=read("Outcome (SUCCESS/RECIPIENT_UNAVAILABLE/ADDRESS_NOT_FOUND/OTHER): ").toUpperCase();
        if(!outcome.matches("SUCCESS|RECIPIENT_UNAVAILABLE|ADDRESS_NOT_FOUND|OTHER")) throw new IllegalArgumentException("Invalid outcome.");
        String notes=read("Notes: ");
        try(Connection c=DB.getConnection()) {
            long id; int next=1;
            try(PreparedStatement ps=c.prepareStatement("SELECT parcel_id FROM parcels WHERE tracking_number=?")) {
                ps.setString(1,tracking); try(ResultSet rs=ps.executeQuery()){if(!rs.next()) throw new IllegalArgumentException("Tracking number not found."); id=rs.getLong(1);}
            }
            try(PreparedStatement ps=c.prepareStatement("SELECT COALESCE(MAX(attempt_no),0)+1 FROM delivery_attempts WHERE parcel_id=?")) {
                ps.setLong(1,id); try(ResultSet rs=ps.executeQuery()){rs.next(); next=rs.getInt(1);}
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO delivery_attempts(parcel_id,attempt_no,outcome,notes) VALUES(?,?,?,?)")) {
                ps.setLong(1,id); ps.setInt(2,next); ps.setString(3,outcome); ps.setString(4,notes); ps.executeUpdate();
            }
            System.out.println("Delivery attempt recorded. Attempt no: "+next);
        }
    }
    private static void trackParcel() throws SQLException {
        String tracking=required("Tracking number: ");
        try(Connection c=DB.getConnection(); PreparedStatement ps=c.prepareStatement(
            "SELECT p.current_status,p.destination_city,p.booked_at,h.status,h.location,h.remarks,h.updated_at FROM parcels p LEFT JOIN tracking_history h ON h.parcel_id=p.parcel_id WHERE p.tracking_number=? ORDER BY h.updated_at")) {
            ps.setString(1,tracking);
            try(ResultSet rs=ps.executeQuery()) {
                boolean found=false;
                while(rs.next()) {
                    if(!found) { System.out.printf("Status: %s | Destination: %s | Booked: %s%n",rs.getString(1),rs.getString(2),rs.getTimestamp(3)); found=true; }
                    if(rs.getString(4)!=null) System.out.printf("%s | %s | %s | %s%n",rs.getTimestamp(7),rs.getString(4),rs.getString(5),rs.getString(6));
                }
                if(!found) System.out.println("Tracking number not found.");
            }
        }
    }
    private static void report() throws SQLException {
        String sql="SELECT current_status,COUNT(*) AS total FROM parcels GROUP BY current_status ORDER BY current_status";
        try(Connection c=DB.getConnection(); Statement s=c.createStatement(); ResultSet rs=s.executeQuery(sql)) {
            System.out.println("=== SHIPMENT SUMMARY ===");
            while(rs.next()) System.out.printf("%-20s %d%n",rs.getString(1),rs.getInt(2));
        }
    }
    private static void addHistory(Connection c,long parcelId,String status,String location,String remarks,Long person) throws SQLException {
        String sql="INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_by_personnel_id) VALUES(?,?,?,?,?)";
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setLong(1,parcelId); ps.setString(2,status); ps.setString(3,location); ps.setString(4,remarks);
            if(person==null) ps.setNull(5,Types.BIGINT); else ps.setLong(5,person);
            ps.executeUpdate();
        }
    }
    private static String required(String label) {
        String value=read(label);
        if(value.isBlank()) throw new IllegalArgumentException("This field cannot be empty.");
        return value;
    }
}
