import os
from pathlib import Path
import pandas as pd
import mysql.connector

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "output"
OUT.mkdir(exist_ok=True)

def get_connection():
    return mysql.connector.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "3306")),
        database=os.getenv("DB_NAME", "courier_tracking"),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", ""),
    )

def main():
    query = '''
        SELECT p.parcel_id, p.tracking_number, c.full_name AS sender_name,
               p.destination_city, p.destination_zone, p.weight_kg,
               p.delivery_charge, p.current_status, p.booked_at,
               p.estimated_delivery_date, p.delivered_at,
               dp.full_name AS assigned_personnel,
               COUNT(DISTINCT da.attempt_id) AS delivery_attempts
        FROM parcels p
        JOIN customers c ON c.customer_id=p.sender_customer_id
        LEFT JOIN delivery_personnel dp ON dp.personnel_id=p.assigned_personnel_id
        LEFT JOIN delivery_attempts da ON da.parcel_id=p.parcel_id
        GROUP BY p.parcel_id, p.tracking_number, c.full_name, p.destination_city,
                 p.destination_zone, p.weight_kg, p.delivery_charge, p.current_status,
                 p.booked_at, p.estimated_delivery_date, p.delivered_at, dp.full_name
    '''
    with get_connection() as conn:
        df = pd.read_sql(query, conn)

    # Standardize text, dates and numeric columns.
    for col in ["tracking_number", "sender_name", "destination_city", "destination_zone", "current_status"]:
        df[col] = df[col].astype("string").str.strip()
    for col in ["booked_at", "estimated_delivery_date", "delivered_at"]:
        df[col] = pd.to_datetime(df[col], errors="coerce")
    for col in ["weight_kg", "delivery_charge", "delivery_attempts"]:
        df[col] = pd.to_numeric(df[col], errors="coerce")

    # Remove exact duplicate rows; tracking number is the business identifier.
    df = df.drop_duplicates()
    df = df.drop_duplicates(subset=["tracking_number"], keep="last")

    # Missing-value treatment is explicit and conservative.
    df["assigned_personnel"] = df["assigned_personnel"].fillna("Unassigned")
    df["delivery_attempts"] = df["delivery_attempts"].fillna(0).astype(int)
    df["weight_kg"] = df["weight_kg"].fillna(df["weight_kg"].median())
    df["delivery_charge"] = df["delivery_charge"].fillna(df["delivery_charge"].median())

    # Derive useful analysis fields without overwriting source columns.
    df["booking_month"] = df["booked_at"].dt.to_period("M").astype("string")
    df["delivery_delay_days"] = (
        df["delivered_at"].dt.normalize() - df["estimated_delivery_date"].dt.normalize()
    ).dt.days
    df["is_delivered"] = df["current_status"].eq("DELIVERED")
    target = OUT / "processed_shipments.csv"
    df.to_csv(target, index=False)
    print(f"Retrieved and processed {len(df)} parcel records.")
    print(f"Processed dataset: {target}")

if __name__ == "__main__":
    main()
