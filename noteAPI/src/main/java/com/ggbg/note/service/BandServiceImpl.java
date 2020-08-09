package com.ggbg.note.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ggbg.note.bean.AccountBand;
import com.ggbg.note.bean.Band;
import com.ggbg.note.exception.InternalServerException;
import com.ggbg.note.repository.AccountBandRepo;
import com.ggbg.note.repository.BandRepo;
import com.ggbg.note.util.JwtTokenUtil;

@Service
public class BandServiceImpl implements IBandService {
	
	@Autowired
	private AccountBandRepo accountBandRepo;
	
	@Autowired
	private BandRepo bandRepo;
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	JwtTokenUtil jtu;

	
	
	@Override
	public Band addBand(String bandName, int accountNo) {
		// band add
		Band band = new Band();
		band.setName(bandName);
		band.setMaster(accountNo);
		
		int bandNo = -1;
		bandNo = bandRepo.save(band).getNo();
		
		if(bandNo == -1)
			throw new InternalServerException("addBand");
		
		band.setNo(bandNo);
		
		AccountBand accountBand = new AccountBand();
		accountBand.setBandNo(bandNo);
		accountBand.setAccountNo(accountNo);
		accountBand.setStatus(0);
		
		int accountBandNo = -1;
		
		accountBandNo = accountBandRepo.save(accountBand).getNo();
		
		if(accountBandNo == -1)
			throw new InternalServerException("addBand");
		
		return band;
	}
	
	@Override
	public int deleteBand(int bandNo, int accountNo) {
		int ret = -1;
		
		Optional<Band> optional = bandRepo.findBandByNo(bandNo);
		if(optional.isPresent()) {
			Band band = optional.get();
			if(band.getMaster() == accountNo)
				ret = bandRepo.deleteBandByNo(bandNo);
		}else {
			return ret;
		}
		return ret;
	}
	
	
	@Override
	public int renameBand(String newBandName, int bandNo, int accountNo) {
		int ret = -1;
		
		Optional<Band> optional = bandRepo.findBandByNo(bandNo);
		if(optional.isPresent()) {
			Band band = optional.get();
			if(band.getMaster() == accountNo)
				ret = bandRepo.updateBandByNo(newBandName, bandNo);
		}else {
			return ret;
		}
		return ret;
	}

}
