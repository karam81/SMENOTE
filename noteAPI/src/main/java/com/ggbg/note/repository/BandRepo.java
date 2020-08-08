package com.ggbg.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ggbg.note.bean.Band;

public interface BandRepo extends JpaRepository<Band, String> {
	@Query(value = "SELECT * "
			+ "FROM band "
			+ "where band_no in (select band_no from account_band where account_no = ?1 and account_status = 2) ", nativeQuery = true)
	List<Band> findAllBandStatusByAccountNo(int no);
	
	@Query(value = "SELECT * "
			+ "FROM band "
			+ "where band_no in (select band_no from account_band where account_no = ?1 and account_status != 2)", nativeQuery = true)
	List<Band> findAllBandByAccountNo(int no);
}