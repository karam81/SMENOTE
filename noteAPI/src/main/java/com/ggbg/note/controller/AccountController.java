package com.ggbg.note.controller;

import java.util.HashMap;
import java.util.List;
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
import com.ggbg.note.domain.dto.AccountDTO;
import com.ggbg.note.domain.dto.BandDTO;
import com.ggbg.note.service.IAccountService;

import io.swagger.annotations.ApiOperation;

@RequestMapping("/account")
@RestController
public class AccountController {
	/*
	 * ?????? -->> 만약에 accessToken 이 만료되지 않은줄 알고 보냈는데 만료가 된 상태라면? -->> 지금은 그냥 10분
	 * 미만으로 내려오거나 이미 만료된 access token 이라면 프론드단에서 액세스 토큰을 재발급 해주고 있음.
	 * 
	 * 
	 * 
	 * success 했는데도 값이 없는경우도 존재함.
	 * 
	 * success 하는 경우와 fail 나는 경우 구분 가능한지
	 * 
	 * 만약 access token 으로 갱신하려했는데 안되면 refresh token 줘야함
	 * 
	 * refresh token 도 안되면 다시 로그인 요청해줘야함
	 */
	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private IAccountService accountService;

//	@ApiOperation(value = "logout", httpMethod = "GET", notes = "Hello this is logout")
//	@GetMapping("/logout")
//	public ResponseEntity logout(@RequestParam(required = true) String email) {
//		logger.debug("=============== signIn entered =============");
//		ResponseEntity response = null;
//		final SuccessResponse result = new SuccessResponse();
//		String msg = accountService.logout(email);
//		if("msg".equals("success")) {
//			result.status = true;
//			result.result = msg;
//			response = new ResponseEntity<>(result, HttpStatus.OK);
//		}else {
//			response = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//		}
//		
//		return response;
//	}

	// 받아야하는것 - access token , id, pw , new pw, new name + header에 Email
	@ApiOperation(value = "accountModify", httpMethod = "POST", notes = "Hello this is accountModify")
	@PostMapping("/v1/modify")
	public ResponseEntity accountModify(HttpServletRequest request,
			@RequestBody(required = true) Map<String, String> map) {
		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();
		String email = map.get("email");
		String password = map.get("password");
		String newPassword = map.get("newPassword");
		String newName = map.get("newName");

		boolean isValidAccount = accountService.validAccountCheck(email, password);

		if (isValidAccount) {
			AccountDTO accountDTO = new AccountDTO();
			accountDTO.setEmail(email);
			accountDTO.setPassword(newPassword);
			accountDTO.setName(newName);
			System.out.println(accountDTO.toString());
			boolean res = accountService.saveAccount(accountDTO);
			if (res) {
				result.status = true;
				result.result = "success";
				response = new ResponseEntity<>(result, HttpStatus.OK);
			} else {
				response = new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // NOT_FOUND
			}
		} else { // 유효한 회원정보가 아닐경우
			result.status = true;
			result.result = "fail";
			response = new ResponseEntity<>(result, HttpStatus.OK);
			return response; // fail이 발생하게 되고, 이 경우에는 비밀번호를 다시 확인하라는 문구를 출력한다.
		}

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.

	// email, password 넘겨주면 됨 + header에 Email
	@ApiOperation(value = "accountDelete", httpMethod = "POST", notes = "Hello this is accountDelete")
	@PostMapping("/v1/delete")
	public ResponseEntity accountDelete(HttpServletRequest request,
			@RequestBody(required = true) Map<String, String> map) {
		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();
		String email = map.get("email");
		String password = map.get("password");
		boolean isValidAccount = accountService.validAccountCheck(email, password);

		if (isValidAccount) {
			boolean res = accountService.deleteAccount(email);
			if (res) {
				result.status = true;
				result.result = "success";
				response = new ResponseEntity<>(result, HttpStatus.OK);
			} else {
				response = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		} else { // 유효한 회원정보가 아닐경우
			result.status = true;
			result.result = "fail";
			response = new ResponseEntity<>(result, HttpStatus.OK);
			return response; // fail이 발생하게 되고, 이 경우에는 비밀번호를 다시 확인하라는 문구를 출력한다.
		}

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.

	// Header에 accessToken을 보내주면 됨.
	// 반환값은 name
	@ApiOperation(value = "onLocalInit", httpMethod = "POST", notes = "Hello this is onLocalInit")
	@PostMapping("/onLocalInit")
	public ResponseEntity onLocalInit(HttpServletRequest request) {
		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();

		String accessToken = request.getHeader("Authorization").substring(7);

		Map<String, Object> map = accountService.onLocalInit(accessToken);
		String name = (String) map.get("name");
		if (name != null && !name.equals("")) {
			result.status = true;
			result.result = "success";
			result.map = map;
			response = new ResponseEntity<>(result, HttpStatus.OK);
		} else {
			result.status = false;
			result.result = "fail";
			response = new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
		}


		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.

	// accessToken을 보내주면 됨.
	// 반환값은 name, email, group, status
	@ApiOperation(value = "onServerInit", httpMethod = "POST", notes = "Hello this is onServerInit")
	@PostMapping("/onServerInit")
	public ResponseEntity onServerInit(HttpServletRequest request) {
		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();

		String accessToken = request.getHeader("Authorization").substring(7);

		Map<String, Object> map = accountService.onServerInit(accessToken);
		
		String name = (String) map.get("name");
		
		if (name != null && !name.equals("")) {
			result.status = true;
			result.result = "success";
			result.map = map;
			response = new ResponseEntity<>(result, HttpStatus.OK);
		} else { 
			result.status = false;
			result.result = "fail";
			response = new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
		}

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.
	
	// accessToken을 보내주면 됨.
	// 반환값은 name, email, group, status
	@ApiOperation(value = "statusList", httpMethod = "POST", notes = "Hello this is statusList")
	@PostMapping("/v1/statusList")
	public ResponseEntity statusList(HttpServletRequest request) {
		ResponseEntity response = null;
		final SuccessResponse result = new SuccessResponse();

		String accessToken = request.getHeader("Authorization").substring(7);

		List<BandDTO> list = accountService.statusList(accessToken);
		
		result.status = true;
		if (!list.isEmpty()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("status", list);
			result.result = "success";
			result.map = map;
		} else { 
			result.result = "emptyData";
		}
		response = new ResponseEntity<>(result, HttpStatus.OK);

		return response;
	} // 만약 Unauthorized가 뜨면 access token 이 변조된것이다. 로그아웃 시켜야함.
	
	
}
