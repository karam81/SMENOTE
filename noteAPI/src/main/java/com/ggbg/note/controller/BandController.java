package com.ggbg.note.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ggbg.note.domain.SuccessResponse;
import com.ggbg.note.domain.dto.BandDTO;
import com.ggbg.note.service.IBandService;

import io.swagger.annotations.ApiOperation;

@RequestMapping("/band")
@RestController
public class BandController {
	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private IBandService bandService;

	// header - email, accesstoken
	// body - bandName, accountNo
	// return - success / error
	@ApiOperation(value = "addBand", httpMethod = "POST", notes = "Hello this is addBand")
	@PostMapping("/v1/addBand")
	public ResponseEntity addBand(HttpServletRequest request, 
			@RequestBody(required = true) Map<String, String> map) {

		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();

		String accessToken = request.getHeader("Authorization").substring(7);
		String bandName = map.get("bandName");
		int accountNo = Integer.parseInt(map.get("accountNo"));
		BandDTO band = bandService.addBand(bandName, accountNo);
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("band", band);
		result.status = true;
		result.result = "success";
		result.map = retMap;
		response = new ResponseEntity<>(result, HttpStatus.OK);

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.

	@ApiOperation(value = "deleteBand", httpMethod = "POST", notes = "Hello this is deleteBand")
	@PostMapping("/v1/deleteBand")
	public ResponseEntity deleteBand(HttpServletRequest request,
			@RequestBody(required = true) Map<String, String> map) {

		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();

		int bandNo = Integer.parseInt(map.get("bandNo"));
		int accountNo = Integer.parseInt(map.get("accountNo"));
		int ret = bandService.deleteBand(bandNo, accountNo);

		result.status = true;
		if (ret == 1) {
			result.result = "success";

		} else {
			result.result = "fail";
		}
		response = new ResponseEntity<>(result, HttpStatus.OK);

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.

	@ApiOperation(value = "renameBand", httpMethod = "POST", notes = "Hello this is renameBand")
	@PostMapping("/v1/renameBand")
	public ResponseEntity renameBand(HttpServletRequest request,
			@RequestBody(required = true) Map<String, String> map) {

		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();

		int bandNo = Integer.parseInt(map.get("bandNo"));
		int accountNo = Integer.parseInt(map.get("accountNo"));
		String newBandName = map.get("newBandName");

		int ret = bandService.renameBand(newBandName, bandNo, accountNo);
		result.status = true;

		if (ret == 1) {
			result.result = "success";
		} else {
			result.result = "fail";
		}
		response = new ResponseEntity<>(result, HttpStatus.OK);

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.

}
