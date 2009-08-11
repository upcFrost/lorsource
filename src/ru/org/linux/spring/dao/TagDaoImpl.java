/*
 * Copyright 1998-2009 Linux.org.ru
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.org.linux.spring.dao;

import java.util.List;
import java.util.Collections;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.apache.commons.lang.mutable.MutableDouble;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Closure;

public class TagDaoImpl {
  private SimpleJdbcTemplate jdbcTemplate;

  public SimpleJdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<TagDTO> getTags(int tagcount) {
    String sql = "select value,counter from tags_values where counter>0 order by counter desc limit ?";
    final MutableDouble maxc = new MutableDouble(1);
    final MutableDouble minc = new MutableDouble(-1);
    List<TagDTO> result = getJdbcTemplate().query(sql, new ParameterizedRowMapper<TagDTO>() {
      @Override
      public TagDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        TagDTO result = new TagDTO();
        result.setValue(rs.getString("value"));
        double counter = Math.log(rs.getInt("counter"));
        result.setCounter(counter);

        if (maxc.doubleValue() < counter){
          maxc.setValue(counter);
        }

        if (minc.doubleValue() < 0 || counter < minc.doubleValue()){
          minc.setValue(counter);
        }

        return result;
      }
    }, tagcount);

    if (minc.doubleValue() < 0){
      minc.setValue(0);
    }

    CollectionUtils.forAllDo(result, new Closure() {
      @Override
      public void execute(Object o) {
        TagDTO tag = (TagDTO) o;
        tag.setWeight((int) Math.round(10*(tag.getCounter() - minc.doubleValue())
          / (maxc.doubleValue() - minc.doubleValue())));
      }
    });

    Collections.sort(result);

    return result;
  }

  public static class TagDTO implements Serializable, Comparable<TagDTO>{
    private Integer weight;
    private String value;
    private double counter;
    private static final long serialVersionUID = -1928783200997267710L;

    public Integer getWeight() {
      return weight;
    }

    public void setWeight(Integer weight) {
      this.weight = weight;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    double getCounter() {
      return counter;
    }

    void setCounter(double counter) {
      this.counter = counter;
    }


    @Override
    public int compareTo(TagDTO o) {
      return value.compareTo(o.value);
    }
  }
}
