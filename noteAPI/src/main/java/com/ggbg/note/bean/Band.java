package com.ggbg.note.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.domain.Persistable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "band")
public class Band implements Persistable<String>{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, name = "band_no")
	private int no;
	
	@Column(nullable = false, name = "band_name")
	private String name;

	@Column(nullable = false, name = "band_master")
	private int master;


	@Override
	public String getId() {
		return this.no + "";
	}

	@Override
	public boolean isNew() {
		return false;
	}
}
