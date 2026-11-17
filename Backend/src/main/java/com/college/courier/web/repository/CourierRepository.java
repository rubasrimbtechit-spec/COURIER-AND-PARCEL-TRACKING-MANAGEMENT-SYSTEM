package com.college.courier.web.repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public class CourierRepository {
 private final JdbcTemplate jdbc; private final NamedParameterJdbcTemplate named;
 public CourierRepository(JdbcTemplate jdbc,NamedParameterJdbcTemplate named){this.jdbc=jdbc;this.named=named;}
 public List<Map<String,Object>> query(String sql,Object... args){return jdbc.queryForList(sql,args);}
 public Map<String,Object> one(String sql,Object... args){return jdbc.queryForMap(sql,args);}
 public int update(String sql,Object... args){return jdbc.update(sql,args);}
 public Long insert(String sql,Object... args){
  var key=new org.springframework.jdbc.support.GeneratedKeyHolder();
  jdbc.update(c->{var ps=c.prepareStatement(sql,java.sql.Statement.RETURN_GENERATED_KEYS);
   for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]); return ps;},key);
  return key.getKey().longValue();
 }
 public <T> T transaction(org.springframework.transaction.support.TransactionCallback<T> work,
   org.springframework.transaction.PlatformTransactionManager manager){
  return new org.springframework.transaction.support.TransactionTemplate(manager).execute(work);
 }
}
