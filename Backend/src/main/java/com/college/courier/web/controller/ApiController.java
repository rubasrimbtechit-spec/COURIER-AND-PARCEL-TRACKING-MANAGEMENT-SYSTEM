package com.college.courier.web.controller;
import com.college.courier.web.service.CourierService;
import com.college.courier.web.model.Requests.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api")
public class ApiController {
 private final CourierService service;
 public ApiController(CourierService service){this.service=service;}
 @GetMapping("/dashboard") public Map<String,Object> dashboard(){return service.dashboard();}
 @GetMapping("/customers") public List<Map<String,Object>> customers(){return service.customers();}
 @PostMapping("/customers") public Map<String,Object> customer(@Valid @RequestBody CustomerRequest r){return service.createCustomer(r);}
 @GetMapping("/personnel") public List<Map<String,Object>> personnel(){return service.personnel();}
 @PostMapping("/personnel") public Map<String,Object> personnel(@Valid @RequestBody PersonnelRequest r){return service.createPersonnel(r);}
 @GetMapping("/parcels") public List<Map<String,Object>> parcels(){return service.parcels();}
 @PostMapping("/parcels") public Map<String,Object> book(@Valid @RequestBody ParcelRequest r){return service.book(r);}
 @PutMapping("/parcels/{tracking}/assignment") public Map<String,Object> assign(@PathVariable String tracking,@Valid @RequestBody AssignmentRequest r){return service.assign(tracking,r);}
 @PutMapping("/parcels/{tracking}/status") public Map<String,Object> status(@PathVariable String tracking,@Valid @RequestBody StatusRequest r){return service.updateStatus(tracking,r);}
 @GetMapping("/tracking/{tracking}") public Map<String,Object> tracking(@PathVariable String tracking){return service.tracking(tracking);}
 @PostMapping("/tracking/{tracking}/attempts") public Map<String,Object> attempt(@PathVariable String tracking,@Valid @RequestBody AttemptRequest r){return service.addAttempt(tracking,r);}
 @GetMapping("/reports") public Map<String,Object> reports(){return service.reports();}
}
