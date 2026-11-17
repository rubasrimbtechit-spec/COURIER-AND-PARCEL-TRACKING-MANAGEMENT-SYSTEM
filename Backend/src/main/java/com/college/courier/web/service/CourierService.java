package com.college.courier.web.service;
import com.college.courier.web.repository.CourierRepository;
import com.college.courier.web.model.Requests.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;
@Service
public class CourierService {
 private final CourierRepository repo; private final TransactionTemplate tx;
 private static final Set<String> ZONES=Set.of("LOCAL","REGIONAL","NATIONAL");
 private static final Set<String> STATUSES=Set.of("BOOKED","PICKED_UP","IN_TRANSIT","OUT_FOR_DELIVERY","DELIVERED","DELIVERY_FAILED","RETURNED");
 private static final Set<String> OUTCOMES=Set.of("SUCCESS","RECIPIENT_UNAVAILABLE","ADDRESS_NOT_FOUND","OTHER");
 public CourierService(CourierRepository repo,PlatformTransactionManager manager){this.repo=repo;this.tx=new TransactionTemplate(manager);}
 public Map<String,Object> dashboard(){
  Map<String,Object> result=new LinkedHashMap<>();
  result.put("customers",repo.one("SELECT COUNT(*) total FROM customers").get("total"));
  result.put("personnel",repo.one("SELECT COUNT(*) total FROM delivery_personnel").get("total"));
  result.put("parcels",repo.one("SELECT COUNT(*) total FROM parcels").get("total"));
  result.put("delivered",repo.one("SELECT COUNT(*) total FROM parcels WHERE current_status='DELIVERED'").get("total"));
  result.put("statuses",repo.query("SELECT current_status status,COUNT(*) total FROM parcels GROUP BY current_status ORDER BY current_status"));
  result.put("recent",repo.query("SELECT tracking_number,receiver_name,destination_city,current_status,booked_at FROM parcels ORDER BY booked_at DESC LIMIT 8"));
  return result;
 }
 public List<Map<String,Object>> customers(){return repo.query("SELECT * FROM customers ORDER BY customer_id DESC");}
 public Map<String,Object> createCustomer(CustomerRequest r){
  Long id=repo.insert("INSERT INTO customers(full_name,phone,email,address_line,city) VALUES(?,?,?,?,?)",r.fullName(),r.phone(),blankNull(r.email()),r.addressLine(),r.city());
  return repo.one("SELECT * FROM customers WHERE customer_id=?",id);
 }
 public List<Map<String,Object>> personnel(){return repo.query("SELECT * FROM delivery_personnel ORDER BY personnel_id DESC");}
 public Map<String,Object> createPersonnel(PersonnelRequest r){
  Long id=repo.insert("INSERT INTO delivery_personnel(full_name,phone,service_area,active) VALUES(?,?,?,?)",r.fullName(),r.phone(),r.serviceArea(),r.active()==null?true:r.active());
  return repo.one("SELECT * FROM delivery_personnel WHERE personnel_id=?",id);
 }
 public List<Map<String,Object>> parcels(){return repo.query("SELECT p.*,c.full_name sender_name,dp.full_name personnel_name FROM parcels p JOIN customers c ON c.customer_id=p.sender_customer_id LEFT JOIN delivery_personnel dp ON dp.personnel_id=p.assigned_personnel_id ORDER BY p.booked_at DESC");}
 public Map<String,Object> book(ParcelRequest r){
  String zone=r.destinationZone().toUpperCase(Locale.ROOT);
  if(!ZONES.contains(zone)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Destination zone must be LOCAL, REGIONAL or NATIONAL");
  BigDecimal charge=TariffCalculator.calculate(r.weightKg(),zone);
  String tracking="CP"+LocalDate.now().toString().replace("-","")+UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase(Locale.ROOT);
  return tx.execute(s->{
   Long id=repo.insert("INSERT INTO parcels(tracking_number,sender_customer_id,receiver_name,receiver_phone,delivery_address,destination_city,destination_zone,weight_kg,delivery_charge,current_status,estimated_delivery_date) VALUES(?,?,?,?,?,?,?,?,?,'BOOKED',?)",
    tracking,r.senderCustomerId(),r.receiverName(),r.receiverPhone(),r.deliveryAddress(),r.destinationCity(),zone,r.weightKg(),charge,LocalDate.now().plusDays(zone.equals("LOCAL")?1:zone.equals("REGIONAL")?3:5));
   repo.update("INSERT INTO tracking_history(parcel_id,status,location,remarks) VALUES(?,'BOOKED',?,'Parcel registered')",id,r.destinationCity());
   return repo.one("SELECT * FROM parcels WHERE parcel_id=?",id);
  });
 }
 public Map<String,Object> assign(String tracking,AssignmentRequest r){
  int n=repo.update("UPDATE parcels SET assigned_personnel_id=? WHERE tracking_number=?",r.personnelId(),tracking);
  if(n==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tracking number not found");
  return repo.one("SELECT * FROM parcels WHERE tracking_number=?",tracking);
 }
 public Map<String,Object> updateStatus(String tracking,StatusRequest r){
  String status=r.status().toUpperCase(Locale.ROOT);
  if(!STATUSES.contains(status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported parcel status");
  return tx.execute(s->{
   Map<String,Object> parcel;
   try{parcel=repo.one("SELECT parcel_id FROM parcels WHERE tracking_number=?",tracking);}
   catch(Exception e){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tracking number not found");}
   repo.update("UPDATE parcels SET current_status=?,delivered_at=? WHERE tracking_number=?",status,status.equals("DELIVERED")?java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()):null,tracking);
   repo.update("INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_by_personnel_id) VALUES(?,?,?,?,?)",parcel.get("parcel_id"),status,r.location(),blankNull(r.remarks()),r.personnelId());
   return repo.one("SELECT * FROM parcels WHERE tracking_number=?",tracking);
  });
 }
 public Map<String,Object> tracking(String tracking){
  List<Map<String,Object>> p=repo.query("SELECT * FROM parcels WHERE tracking_number=?",tracking);
  if(p.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tracking number not found");
  Map<String,Object> result=new LinkedHashMap<>();result.put("parcel",p.get(0));
  result.put("history",repo.query("SELECT h.*,dp.full_name personnel_name FROM tracking_history h LEFT JOIN delivery_personnel dp ON dp.personnel_id=h.updated_by_personnel_id WHERE h.parcel_id=? ORDER BY h.updated_at,h.history_id",p.get(0).get("parcel_id")));
  result.put("attempts",repo.query("SELECT * FROM delivery_attempts WHERE parcel_id=? ORDER BY attempt_no",p.get(0).get("parcel_id")));
  return result;
 }
 public Map<String,Object> addAttempt(String tracking,AttemptRequest r){
  String outcome=r.outcome().toUpperCase(Locale.ROOT);
  if(!OUTCOMES.contains(outcome)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid delivery attempt outcome");
  return tx.execute(s->{
   Map<String,Object> p;
   try{p=repo.one("SELECT parcel_id FROM parcels WHERE tracking_number=?",tracking);}
   catch(Exception e){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tracking number not found");}
   Long id=((Number)p.get("parcel_id")).longValue();
   int next=((Number)repo.one("SELECT COALESCE(MAX(attempt_no),0)+1 next_no FROM delivery_attempts WHERE parcel_id=?",id).get("next_no")).intValue();
   Long attempt=repo.insert("INSERT INTO delivery_attempts(parcel_id,attempt_no,outcome,notes) VALUES(?,?,?,?)",id,next,outcome,blankNull(r.notes()));
   return repo.one("SELECT * FROM delivery_attempts WHERE attempt_id=?",attempt);
  });
 }
 public Map<String,Object> reports(){
  Map<String,Object> m=new LinkedHashMap<>();
  m.put("byStatus",repo.query("SELECT current_status status,COUNT(*) total FROM parcels GROUP BY current_status ORDER BY current_status"));
  m.put("byCity",repo.query("SELECT destination_city city,COUNT(*) total FROM parcels GROUP BY destination_city ORDER BY total DESC"));
  m.put("monthly",repo.query("SELECT DATE_FORMAT(booked_at,'%Y-%m') month,COUNT(*) total FROM parcels GROUP BY DATE_FORMAT(booked_at,'%Y-%m') ORDER BY month"));
  m.put("recentAttempts",repo.query("SELECT a.*,p.tracking_number FROM delivery_attempts a JOIN parcels p ON p.parcel_id=a.parcel_id ORDER BY attempted_at DESC LIMIT 20"));
  return m;
 }
 private String blankNull(String s){return s==null||s.isBlank()?null:s.trim();}
}
