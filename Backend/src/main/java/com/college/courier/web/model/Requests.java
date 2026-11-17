package com.college.courier.web.model;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public final class Requests {
 private Requests(){}
 public record CustomerRequest(@NotBlank @Size(max=100) String fullName,@NotBlank @Size(max=20) String phone,
  @Email @Size(max=120) String email,@NotBlank @Size(max=200) String addressLine,@NotBlank @Size(max=80) String city){}
 public record PersonnelRequest(@NotBlank @Size(max=100) String fullName,@NotBlank @Size(max=20) String phone,
  @NotBlank @Size(max=100) String serviceArea, Boolean active){}
 public record ParcelRequest(@NotNull @Positive Long senderCustomerId,@NotBlank @Size(max=100) String receiverName,
  @NotBlank @Size(max=20) String receiverPhone,@NotBlank @Size(max=250) String deliveryAddress,
  @NotBlank @Size(max=80) String destinationCity,@NotBlank String destinationZone,@NotNull @DecimalMin("0.01") BigDecimal weightKg){}
 public record AssignmentRequest(@NotNull @Positive Long personnelId){}
 public record StatusRequest(@NotBlank String status,@NotBlank @Size(max=120) String location,
  @Size(max=250) String remarks,Long personnelId){}
 public record AttemptRequest(@NotBlank String outcome,@Size(max=250) String notes){}
}
