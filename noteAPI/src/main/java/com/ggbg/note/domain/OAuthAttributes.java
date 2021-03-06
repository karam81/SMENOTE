package com.ggbg.note.domain;

import java.util.Map;

import com.ggbg.note.domain.entity.AccountEntity;

public class OAuthAttributes {
   private Map<String, Object> attributes;
   private String nameAttributeKey;
   private String name;
   private String email;
   private String picture;

   public OAuthAttributes() {}
   
   public OAuthAttributes(Map<String, Object> attributes,
                          String nameAttributeKey, String name,
                          String email, String picture) {
       this.attributes = attributes;
       this.nameAttributeKey= nameAttributeKey;
       this.name = name;
       this.email = email;
       this.picture = picture;
   }

   
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getNameAttributeKey() {
		return nameAttributeKey;
	}

	public void setNameAttributeKey(String nameAttributeKey) {
		this.nameAttributeKey = nameAttributeKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public static OAuthAttributes of(String registrationId, String userNameAttributeName,
			Map<String, Object> attributes) {
		System.out.println(registrationId);
		if ("naver".equals(registrationId)) {
			return ofNaver("id", attributes);
		} else if ("kakao".equals(registrationId)) {
			return ofKakao("id", attributes);
		} else if ("google".equals(registrationId)) {
			return ofGoogle(userNameAttributeName, attributes);
		}
		return null;
	}

   private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                           Map<String, Object> attributes) {
	   OAuthAttributes oAuthAttributes = new OAuthAttributes();
	   oAuthAttributes.setName((String)attributes.get("name"));
	   oAuthAttributes.setEmail((String) attributes.get("email"));
	   oAuthAttributes.setPicture((String) attributes.get("picture"));
	   oAuthAttributes.setAttributes(attributes);
	   oAuthAttributes.setNameAttributeKey(userNameAttributeName);
	   return oAuthAttributes;
   }

   private static OAuthAttributes ofNaver(String userNameAttributeName,
           							Map<String, Object> attributes) {
	   	Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		OAuthAttributes oAuthAttributes = new OAuthAttributes();
		oAuthAttributes.setName((String) response.get("name"));
		oAuthAttributes.setEmail((String) response.get("email"));
		oAuthAttributes.setPicture((String) response.get("profile_image"));
		oAuthAttributes.setAttributes(response);
		oAuthAttributes.setNameAttributeKey(userNameAttributeName);
	   	return oAuthAttributes;
   }
   
   private static OAuthAttributes ofKakao(String userNameAttributeName,
				Map<String, Object> attributes) {
	   	Map<String, Object> response = (Map<String, Object>) attributes.get("response");
	   	OAuthAttributes oAuthAttributes = new OAuthAttributes();
		oAuthAttributes.setName((String) response.get("name"));
		oAuthAttributes.setEmail((String) response.get("email"));
		oAuthAttributes.setPicture((String) response.get("profile_image"));
		oAuthAttributes.setAttributes(response);
		oAuthAttributes.setNameAttributeKey(userNameAttributeName);
	   	return oAuthAttributes;
   }

   public AccountEntity toEntity() {
	   AccountEntity account = new AccountEntity();
	   account.setEmail(email);
	   account.setRole(Role.USER);
	   return account;
   }
}