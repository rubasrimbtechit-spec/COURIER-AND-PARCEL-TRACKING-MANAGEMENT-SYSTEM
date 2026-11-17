package com.college.courier.web.controller;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e){
  List<String> errors=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).toList();
  return ResponseEntity.badRequest().body(Map.of("error","Validation failed","details",errors));
 }
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<Map<String,Object>> conflict(DataIntegrityViolationException e){
  return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Database constraint violation. Check duplicate phone numbers and referenced IDs."));
 }
 @ExceptionHandler(Exception.class)
 public ResponseEntity<Map<String,Object>> other(Exception e){
  if(e instanceof org.springframework.web.server.ResponseStatusException r)
   return ResponseEntity.status(r.getStatusCode()).body(Map.of("error",r.getReason()==null?"Request failed":r.getReason()));
  return ResponseEntity.status(500).body(Map.of("error","An unexpected server error occurred."));
 }
}
