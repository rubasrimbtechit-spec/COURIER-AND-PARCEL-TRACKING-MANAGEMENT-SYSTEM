# REST API reference

Base path: `/api`. JSON request/response bodies. The static website is served from `/`.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/dashboard` | Dashboard counts, status summary, recent parcels |
| GET, POST | `/api/customers` | List/create customers |
| GET, POST | `/api/personnel` | List/create delivery personnel |
| GET, POST | `/api/parcels` | List/book parcels |
| PUT | `/api/parcels/{tracking}/assignment` | Assign personnel (`personnelId`) |
| PUT | `/api/parcels/{tracking}/status` | Update status and append tracking history |
| GET | `/api/tracking/{tracking}` | Parcel, chronological history and delivery attempts |
| POST | `/api/tracking/{tracking}/attempts` | Record a delivery attempt |
| GET | `/api/reports` | Status, city, monthly and recent attempt reports |

Valid destination zones: `LOCAL`, `REGIONAL`, `NATIONAL`.
Valid statuses: `BOOKED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RETURNED`.
Valid attempt outcomes: `SUCCESS`, `RECIPIENT_UNAVAILABLE`, `ADDRESS_NOT_FOUND`, `OTHER`.

Example booking JSON:
```json
{
  "senderCustomerId": 1,
  "receiverName": "Priya",
  "receiverPhone": "9200000001",
  "deliveryAddress": "10 Main Road",
  "destinationCity": "Erode",
  "destinationZone": "LOCAL",
  "weightKg": 1.5
}
```
