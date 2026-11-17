from pathlib import Path
import pandas as pd
import matplotlib.pyplot as plt

HERE = Path(__file__).resolve().parent
DATA = HERE.parent / "FDS-Python" / "output" / "processed_shipments.csv"
OUT = HERE / "output"
OUT.mkdir(exist_ok=True)

def save_bar(series, title, xlabel, ylabel, filename, rotation=0):
    ax = series.plot(kind="bar", figsize=(9, 5))
    ax.set_title(title); ax.set_xlabel(xlabel); ax.set_ylabel(ylabel)
    plt.xticks(rotation=rotation, ha="right")
    plt.tight_layout(); plt.savefig(OUT / filename, dpi=160); plt.close()

def main():
    if not DATA.exists():
        raise SystemExit("Processed dataset not found. Run FDS-Python/retrieve_preprocess.py first.")
    df = pd.read_csv(DATA, parse_dates=["booked_at", "estimated_delivery_date", "delivered_at"])
    if df.empty:
        raise SystemExit("Processed dataset contains no rows; charts were not generated.")

    monthly = df.groupby(df["booked_at"].dt.to_period("M")).size()
    ax = monthly.plot(kind="line", marker="o", figsize=(9, 5))
    ax.set_title("Monthly Shipment Trend"); ax.set_xlabel("Booking month"); ax.set_ylabel("Shipments")
    plt.tight_layout(); plt.savefig(OUT / "monthly_shipment_trend.png", dpi=160); plt.close()

    status_counts = df["current_status"].value_counts()
    save_bar(status_counts, "Shipment Volume by Current Status", "Status", "Shipments",
             "shipment_volumes_by_status.png", 25)

    completion = 100 * df["is_delivered"].astype(bool).mean()
    plt.figure(figsize=(6, 4))
    plt.bar(["Delivered", "Not delivered"], [completion, 100-completion])
    plt.ylim(0, 100); plt.ylabel("Percentage"); plt.title("Delivery Completion Rate")
    plt.tight_layout(); plt.savefig(OUT / "delivery_completion_rate.png", dpi=160); plt.close()

    delays = df.loc[df["delivery_delay_days"].notna(), "delivery_delay_days"]
    if not delays.empty:
        plt.figure(figsize=(8, 5))
        plt.hist(delays, bins=min(10, max(3, len(delays))), edgecolor="black")
        plt.title("Delivery Delay Distribution"); plt.xlabel("Days (negative = early)"); plt.ylabel("Parcels")
        plt.tight_layout(); plt.savefig(OUT / "delivery_delay_distribution.png", dpi=160); plt.close()

    city_volume = df["destination_city"].value_counts().head(10)
    save_bar(city_volume, "Top Destination Cities by Shipment Volume", "City", "Shipments",
             "destination_city_volumes.png", 30)
    print(f"Charts saved to: {OUT}")

if __name__ == "__main__":
    main()
