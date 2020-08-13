import Vue from 'vue'
import Vuex from 'vuex'

import axios from 'axios'
import SERVER from '@/api/spring'

Vue.use(Vuex);

export default new Vuex.Store({
  
  // data의 집합(중앙 관리할 모든 데이터 === 상태)
  state: {

    // auth_token
    authorization: localStorage.getItem('authorization'),
    accessTokenExpiraionDate: localStorage.getItem('access-token-expiraion-date'),
    refreshToken: localStorage.getItem('refresh-token'),
    refreshTokenExpiraionDate: localStorage.getItem('refresh-token-expiraion-date'),

    // user info
    userInfo: {
      email: '',
      name: '',
      group: [],
      status: [],
      no: ''
    },

    // auth_check
    isDuplicateChecked: false,
    isAuthNumChecked: false,
    isPasswordChecked: false,
    isModifyChecked: false,

    // modal
    drawer: false,
    drawerShare: false,
    isLogoutModal: false,
    isMypageModal: false,
    isDeleteModal: false,
    isInviteModal: false,
    noSuchMemberAlert: false,

    // server_check
    isShareMode: false,

    // selected workspace in server mode
    workspace: '',
    workspaceMemberList: [],
    newMemberInfo: {
      email: '',
      name: '',
      status: '',
      no: ''
    },
    fileList: [
      {
        no: 1,
        bandNo: 1,
        title: '낄낄.md',
        contents: '# 낄낄\n넘모좋군  낄낄'
      },
      {
        no: 2,
        bandNo: 1,
        title: '깔깔.md',
        contents: '# 깔깔\n넘모좋군  깔깔'
      },
      {
        no: 3,
        bandNo: 1,
        title: '꼴깔.md',
        contents: '# 꼴깔\n넘모좋군  꼴깔꼴깔'
      },
    ],

    theme: '',
  },

  // state를 (가공해서)가져올 함수들. === computed
  getters: {
    isLoggedIn: state => !!state.authorization,
    status: state => state.userInfo.status
    // getWorkspaceMemberList: state => {
    //   return state.workspaceMemberList
    // },

    // config: state => ({
    //   headers: {
    //     Authorization:  state.authorization,
    //     RefreshToken: state.refreshToken,
    //     Email: state.userInfo.email,
    //   }
    // }),
  },

  // state를 변경하는 함수들(mutations에 작성되지 않은 state 변경 코드는 모두 동작하지 않음.)
  // 모든 mutation 함수들은 동기적으로 동작하는 코드.all
  // commit 을 통해 실행함.
  // mutations은 첫 번째 인자로 state를 받아야함.
  mutations: {

    // 토큰 저장
    SET_TOKEN(state, token) {
      if (!!token.authorization) {
        state.authorization = token.authorization
        localStorage.setItem('authorization', state.authorization)
      }
      if (!!token.accesstokenexpiraiondate) {
        state.accessTokenExpiraionDate = token.accesstokenexpiraiondate
        localStorage.setItem('access-token-expiraion-date', state.accessTokenExpiraionDate)
      }
      if (!!token.refreshtoken) {
        state.refreshToken = token.refreshtoken
        localStorage.setItem('refresh-token', state.refreshToken)
      }
      if (!!token.refreshtokenexpiraiondate) {
        state.refreshTokenExpiraionDate = token.refreshtokenexpiraiondate
        localStorage.setItem('refresh-token-expiraion-date', state.refreshTokenExpiraionDate)
      }
    },

    // 토큰 삭제
    DELETE_TOKEN(state) {
      state.authorization = null
      state.accessTokenExpiraionDate = null
      state.refreshToken = null
      state.refreshTokenExpiraionDate = null
      state.userInfo.name = null
    },

    // email 중복체크 결과 저장
    SET_DUPLICATE_CHECKED(state, result) {
      state.isDuplicateChecked = result
    },

    // 인증번호 확인 결과 저장
    SET_AUTHNUM_CHECKED(state, result) {
      result==='success' ? state.isAuthNumChecked = true : state.isAuthNumChecked = false
    },

    // 비밀번호 확인 결과 저장
    SET_PASSWORD_CHECKED(state, result) {
      state.isPasswordChecked = result
    },

    // 회원정보 수정 결과 저장
    SET_MODIFY_RESULT(state, result) {
      state.isModifyChecked = result
    },

    // 초기 회원정보 저장
    SET_INIT_USER_INFO(state, info) {
      state.userInfo = info
    },

    // 워크스페이스 저장
    SET_WORKSPACES(state, result) {
      state.userInfo.group.push(result);
      console.log("state.userInfo.group : ", state.userInfo.group);
    },

    // 선택한 워크스페이스
    SELET_WORKSPACE(state, payload) {
      state.workspace = payload
    },
    
    DELETE_WORKSPACE(state, param) {
      console.log("DELETE_WORKSPACE 호출됨.")
      console.log(param)
      console.log(param.workspaceIdx)
      var idx = param.workspaceIdx;
      console.log(idx + "번째 아이템 : " + state.userInfo.group[idx].name + "[삭제]");
      state.userInfo.group.splice(idx, 1)
    },

    UPDATE_WORKSPACE(state, param) {
      console.log("UPDATE_WORKSPACE 호출됨.")
      console.log(param)
      console.log(param.workspaceIdx)
      var idx = param.workspaceIdx
      console.log(idx + "번째 아이템 : " + state.userInfo.group[idx].name + "[변경]")
      state.userInfo.group[idx].name = param.newBandName
    },
    
    
    //현재 WORKSPACE 내의 MEMBER LIST 가져오기
    SHOW_GROUP_MEMBERS(state, result) {
      console.log('result', result)
      for (let i = 0; i < result.length; i += 1) {
        if (typeof (result[i]) === 'object') {
          try {
            state.workspaceMemberList[i] = JSON.parse(JSON.stringify(result[i]));
          } catch (e) {
            console.error(e);
          }
        }
      }
      // state.workspaceMemberList.push(result)
      console.log(state.workspaceMemberList)
    },

    GET_NEW_MEMBER_INFO(state, result) {
      state.workspaceMemberList.push(result);
      console.log(state.workspaceMemberList)
    },

    SET_IS_SHARE(state, result) {
      state.isShareMode = result
    },

    SET_IS_DRAWER(state, result) {
      state.drawer = result
    },

    SET_IS_DRAWER_SHARE(state, result) {
      state.drawerShare = result
    },

    // 초기 fileList 정보 저장
    INIT_FILE_LIST(state, payload) {
      state.fileList = payload
    },

    // FileList 에 File 추가하기
    SET_FILE_INFO(state, payload) {
      state.fileList.push(payload);
    },
  },

  // 범용적인 함수들. mutations에 정의한 함수를 actions에서 실행 가능.
  // 비동기 로직은 actions에서 정의.
  // dispatch를 통해 실행함.
  actions: {

    // 로그인
    login({ commit, dispatch }, loginData) {
      axios.post(SERVER.URL + SERVER.ROUTES.login, loginData)
        .then(res => {
          console.log(res.headers)
          commit('SET_TOKEN', res.headers)  // 토큰 저장
          commit('SET_PASSWORD_CHECKED', false)

          /* 무성 추가 부분 -> 로그인을 한 후에도 서버요청해서 이름 정보 가져와야 할 듯 싶어서 추가 */
          /* 로그인을 해도 소망이님이 뜨고, 로그인상태로 앱을 껏다 켜야 yb님이라고 뜸. 애초에 로그인 후에 바로 가져오는 게 맞을 것 같습니다. */
          dispatch('initUserInfo');
          setTimeout(function(){
            commit('SET_IS_SHARE', true);
            }, 70);
        })
        .catch(err => {
          console.error(err.response.data)
          if (err.response.status===401) {
            commit('SET_PASSWORD_CHECKED', true)
          }
        })
    },
    
    // 회원가입
    signup({ dispatch }, signupData) {
      axios.post(SERVER.URL + SERVER.ROUTES.signup, signupData)
        .then(() => {
          const loginData = {
            email: signupData.email,
            password: signupData.password 
          }
          dispatch('login', loginData)  // 회원가입 성공 시, 자동 로그인
        })
        .catch(err => console.error(err.response.data))
    },

    // 로그아웃
    logout({ commit }) {
      axios.post(SERVER.URL + SERVER.ROUTES.logout)
        .then(() => {
          commit('DELETE_TOKEN')  // state 에서도 삭제

          // Local Storage 에서도 삭제
          localStorage.removeItem('authorization');
          localStorage.removeItem('access-token-expiraion-date')
          localStorage.removeItem('refresh-token')
          localStorage.removeItem('refresh-token-expiraion-date')
          // localStorage.removeItem('email')
          
          /* 서버모드로 켜놓고, 로그아웃 하면 서버모드가 유지됩니다. */
          /* 로그아웃시 로컬모드만 사용할 수 있도록 false로 고정해놨습니다. */
          commit('SET_IS_SHARE', false)
        })
        .catch(err => console.error(err.response.data))
    },

    // 이메일 중복확인
    checkEmailDuplicate({ commit, dispatch }, signupData) {
      axios.post(SERVER.URL + SERVER.ROUTES.email, signupData)
        .then(res => {
          
          // 사용 가능한 이메일 일 때,
          if (res.data['result'] === 'success') {
            commit('SET_DUPLICATE_CHECKED', true)
            dispatch('sendAuthNum', signupData)  // 사용 가능한 이메일이면, 바로 인증번호 이메일로 전송
          }

          // 이미 가입된 이메일 일때,
          else {
            commit('SET_DUPLICATE_CHECKED', false)
          }
        })
        .catch(err => console.error(err.response.data))
    },

    // 이메일로 인증번호 보내기
    sendAuthNum({ state }, signupData) {

      // 이메일 중복 검사가 확인되었을 때만 실행
      if (state.isDuplicateChecked) {
        axios.post(SERVER.URL + SERVER.ROUTES.authSend, signupData)
          .then().catch(err => console.error(err.response.data))
      }
    },

    // 인증번호 확인
    checkAuthNum({ commit }, signupData) {
      axios.post(SERVER.URL + SERVER.ROUTES.authCheck, signupData)
        .then(res => {
          commit('SET_AUTHNUM_CHECKED', res.data['result'])  // 인정번호 확인 결과 저장
        })
        .catch(err => console.error(err.response.data))
    },

    // 회원정보 수정
    updateUserInfo({ commit, dispatch }, userInfo) {
      axios.post(SERVER.URL + SERVER.ROUTES.modify, userInfo, { headers: { email: userInfo.email } })
        .then(res => {
          
          if (res.data['result'] === 'success') {
            commit('SET_MODIFY_RESULT', false)
            dispatch('initUserInfo');
          }

          else {
            commit('SET_MODIFY_RESULT', true)
          }
        })
        .catch(err => console.error(err.response.data))
    },

    // 회원탈퇴
    deleteAccount({ commit, dispatch }, userInfo) {
      axios.post(SERVER.URL + SERVER.ROUTES.delete, userInfo, { headers: { email: userInfo.email } })
        .then(res => {

          if (res.data['result'] === 'success') {
            commit('SET_MODIFY_RESULT', false)
            dispatch('logout')
          }

          else {
            commit('SET_MODIFY_RESULT', true)
          }
        })
        .catch(err => console.error(err.response.data))
    },

    // 초기 회원정보 세팅
    initUserInfo({ commit }) {
      axios.post(SERVER.URL + SERVER.ROUTES.onServerInit)
        .then(res => {
          if (res.data['result'] === 'success') {
            console.log("################# res.data.map", res.data.map)
            commit('SET_INIT_USER_INFO', res.data.map)
          }
        })
        .catch(err => console.error(err.response.data))
    },

    // 워크스페이스 생성
    createWorkspace({ commit }, workspaceName) {
      // console.log("Vuex내에 createWorkspace() 함수 진입.");
      // console.log("bandName : " + workspaceName)
      // console.log("accountNo : " + this.state.userInfo.no);

      var map = {
        bandName: workspaceName,
        accountNo: this.state.userInfo.no,
        bandMasterName: this.state.userInfo.name
      }
    
      axios.post(SERVER.URL + SERVER.ROUTES.createWorkspace, map, { headers: { email: this.state.userInfo.email }})
      .then(res => {
        console.log("then구문 진입.");
        console.log("res.data", res.data);
        console.log("res.data.map", res.data.map);
        console.log("res.data.map.band", res.data.map.band);

        commit("SET_WORKSPACES", res.data.map.band)
        // console.log("then구문 진입.");
        
        // commit("SET_WORKSPACES", res.data.map)
      })
      .catch(err => {

      })
    },

    // 워크스페이스 제거
    deleteWorkspace({ commit }, deleteWorkspace) {
      console.log("Vuex내에 deleteWorkspace() 진입.");
      console.log("넘어온 그룹 정보 (bandNo, accountNo) : ", deleteWorkspace)
      
      axios.post(SERVER.URL + SERVER.ROUTES.deleteWorkspace, deleteWorkspace, { headers: { email: this.state.userInfo.email }})
      .then(res => {
        console.log("res.data.result : ", res.data.result)
        if(res.data.result == "success") {
          console.log("success안에 들어옴.")
          commit("DELETE_WORKSPACE", deleteWorkspace)

        } else if(res.data.result == "fail") {

        }
      })
      .catch(err => {

      })
    },

    // 워크스페이스명 변경
    renameWorkspace({ commit }, renameWorkspace) {
      console.log("Vuex내에 renameWorkspace() 진입")
      console.log("넘어온 그룹 정보 (bandNo, accountNo, newBandName, workspaceIdx) :", renameWorkspace)

      axios.post(SERVER.URL + SERVER.ROUTES.renameWorkspace, renameWorkspace, { headers: { email: this.state.userInfo.email }})
      .then(res => {
        console.log("res.data.result : ", res.data.result)
        if(res.data.result == "success") {
          commit("UPDATE_WORKSPACE", renameWorkspace)
        }

      })
    },
    // 워크스페이스 멤버 불러오기
    showGroupMembers({ commit }, showGroupMembers) {
      // console.log(showGroupMembers)
      axios.post(SERVER.URL + SERVER.ROUTES.getBandMember, showGroupMembers, { headers: { email: this.state.userInfo.email }})
      .then(res => {
        console.log("res.data.result : ", res.data.result)
        commit("SHOW_GROUP_MEMBERS", res.data.map.bandMemberList)
        this.state.isInviteModal = !(this.state.isInviteModal)
        })
    },

    // 가입된 회원인지 확인
    findAccountList({ dispatch }, findAccountList) {
      axios.post(SERVER.URL + SERVER.ROUTES.findAccountList, findAccountList, { headers: { email: this.state.userInfo.email }})
      .then(res => {
        this.state.newMemberInfo.no = res.data.map.primitiveAccountList[0].no; // 초대받을 사람의 account_no를 보관.
        // console.log("res.data.map.primitiveAccountList[0].no : ", res.data.map.primitiveAccountList[0].no);
        if (res.data.result === "success") {
          const inviteBandMember = {
            bandNo: this.state.userInfo.group.find(element => element.name == this.state.workspace).no,
            email: findAccountList.email,
            masterNo: this.state.userInfo.group.find(element => element.name == this.state.workspace).master,
          }
          // console.log("[inviteBandMember] findAccountList()", inviteBandMember)
          dispatch("inviteBandMember", inviteBandMember)
        } else {
          this.state.noSuchMemberAlert = !(this.state.noSuchMemberAlert)
        }
      })
    },

    // 워크스페이스에 멤버 초대하기
    inviteBandMember({ commit }, inviteBandMember) {
      console.log("[inviteBandMember] inviteBandMember()", inviteBandMember);

      this.state.noSuchMemberAlert = false;
      axios.post(SERVER.URL + SERVER.ROUTES.inviteBandMember, inviteBandMember, { headers: { email: this.state.userInfo.email} })
      .then(res => {
        console.log("res.data.result : ", res.data.map.bandMember)
        commit("GET_NEW_MEMBER_INFO", res.data.map.bandMember)
      })
    },

    // 워크스페이스 초대 수락
    acceptInvite({}, info) {
      console.log("[acceptInvite] info : ", info)
      axios.post(SERVER.URL + SERVER.ROUTES.acceptInvite, info, { headers: { email: this.state.userInfo.email } })
      .then(res => {
        console.log("초대 수락 확인")
      }) 
    },

    // 워크스페이스 초대 거절
    declineInvite({}, info) {
      console.log("[declineInvite] info : ", info)
      axios.post(SERVER.URL + SERVER.ROUTES.declineInvite, info, { headers: { email: this.state.userInfo.email } })
      .then(res => {
        console.log("초대 거부 확인")
      })
    },

    // showGroupMembers({ commit }, showGroupMembers) {
    //   // console.log(showGroupMembers)
    //   axios.post(SERVER.URL + SERVER.ROUTES.getBandMember, showGroupMembers, { headers: { email: this.state.userInfo.email }})
    //   .then(res => {
    //     console.log("res.data.result : ", res.data.result)
    //     commit("SHOW_GROUP_MEMBERS", res.data.map.bandMemberList)
    //     this.state.isInviteModal = !(this.state.isInviteModal)
    //     })
    // },

    // fileList 조회
    showFileList({ state, commit }, seletedBandName) {
      const info = {
        accountNo: state.userInfo.no,
        bandNo: state.userInfo.group.find(element => element.name === seletedBandName).no,
      }
      axios.post(SERVER.URL + SERVER.ROUTES.fileList, info, { headers: { email: state.userInfo.email } })
      .then(res => {
        commit('INIT_FILE_LIST', res.data)
      })
      .catch(err => console.error(err.response.data))
    },

    // file 추가
    createFile({ state, commit }, fileData) {
      const info = {
        accountNo: state.userInfo.no,
        bandNo: state.userInfo.group.find(element => element.name === state.workspace).no,
        // bandNo: 1,
        title: fileData,
      }
      axios.post(SERVER.URL + SERVER.ROUTES.createFile, info, { headers: { email: state.userInfo.email } })
      .then(res => {
        commit('SET_FILE_INFO', res.data)
      })
      .catch(err => console.error(err.response.data))
    },


    deleteFile({ state, commit }, fileData) {
      const info = {
        bandNo: state.userInfo.group.find(element => element.name === state.workspace).no,
        // bandNo: 1,
        accountNo: state.userInfo.no,
        title: fileData,
      }
      axios.post(SERVER.URL + SERVER.ROUTES.createFile, info, { headers: { email: state.userInfo.email } })
      .then(res => {
        commit('SET_FILE_INFO', res.data)
      })
      .catch(err => console.error(err.response.data))
    },
  },
  modules: {}
});