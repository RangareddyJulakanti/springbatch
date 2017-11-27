package com.cvs.example.springbatch.mapper;

import com.cvs.example.springbatch.model.ExamResult;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExamResultRowMapper implements RowMapper<ExamResult>{

	@Override
	public ExamResult mapRow(ResultSet rs, int rowNum) throws SQLException {
		ExamResult result = new ExamResult();
		result.setStudentName(rs.getString("student_name"));
		result.setDob(new LocalDate(rs.getDate("dob")));
		result.setPercentage(rs.getInt("percentage"));
		return result;
	} 

}
