<template>
  <div class="login-container">
    <transition name="el-zoom-in-center" mode="out-in">
      
      <el-card v-if="mode === 'login'" key="login" class="action-card" shadow="always">
        <template #header>
          <h2 class="sys-title">🏥 智慧医院全场景系统</h2>
        </template>

        <el-form :model="loginForm" size="large">
          <el-form-item>
            <el-input v-model="loginForm.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="full-btn" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>

        <div class="card-footer">
          <span>还没有账号？</span>
          <el-button type="primary" link @click="mode = 'register'">立即注册</el-button>
        </div>

        <el-divider>超级管理员专属通道</el-divider>
        <el-button type="danger" plain class="full-btn" @click="quickLogin('admin')">一键登录 Admin</el-button>
      </el-card>

      <el-card v-else key="register" class="action-card register-card" shadow="always">
        <template #header>
          <div class="register-header">
            <h2 class="sys-title">✨ 欢迎注册</h2>
            <el-button type="info" link @click="mode = 'login'">返回登录</el-button>
          </div>
        </template>

        <el-tabs v-model="registerRole" stretch>
          <el-tab-pane label="我是患者" name="PATIENT">
            <el-form :model="regForm" label-position="top" size="default">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="登录账号"><el-input v-model="regForm.username" placeholder="建议使用字母数字" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="登录密码"><el-input v-model="regForm.password" type="password" show-password /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="真实姓名"><el-input v-model="regForm.realName" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="手机号"><el-input v-model="regForm.phone" maxlength="11" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="身份证号">
                <el-input v-model="regForm.idCard" maxlength="18" />
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-radio-group v-model="regForm.gender">
                      <el-radio :value="1">男</el-radio>
                      <el-radio :value="2">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出生日期">
                    <input 
                      type="date" 
                      v-model="regForm.birthDate" 
                      class="el-input__inner" 
                      style="width: 100%; height: 32px; border: 1px solid #dcdfe6; border-radius: 4px; padding: 0 11px; color: #606266; outline: none;" 
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-button type="success" class="full-btn mt-4" :loading="loading" @click="submitRegister">注册患者账号</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="我是医生" name="DOCTOR">
            <el-form :model="regForm" label-position="top" size="default">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="登录账号"><el-input v-model="regForm.username" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="登录密码"><el-input v-model="regForm.password" type="password" show-password /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="医生姓名"><el-input v-model="regForm.realName" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="手机号"><el-input v-model="regForm.phone" maxlength="11" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属科室">
                    <el-select v-model="regForm.deptId" placeholder="选择科室" style="width: 100%">
                      <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="职称 (如:主任医师)"><el-input v-model="regForm.title" /></el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-radio-group v-model="regForm.gender">
                      <el-radio :value="1">男</el-radio>
                      <el-radio :value="2">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="从业年限"><el-input-number v-model="regForm.experienceYears" :min="0" style="width: 100%" /></el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="擅长领域 (如: 心血管内科常见病)">
                <el-input v-model="regForm.expertise" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="regForm.introduction" type="textarea" :rows="2" />
              </el-form-item>
              <el-button type="warning" class="full-btn mt-4" :loading="loading" @click="submitRegister">入驻医生工作台</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>

    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)

const mode = ref('login')
const registerRole = ref('PATIENT')
const deptList = ref<any[]>([])

const loginForm = reactive({
  username: '',
  password: ''
})

const regForm = reactive({
  username: '', password: '', realName: '', phone: '', gender: 1,
  idCard: '', birthDate: '', 
  deptId: null, title: '', experienceYears: 0, expertise: '', introduction: ''
})

onMounted(async () => {
  try {
    const res: any = await request.get('/api/dept/list')
    deptList.value = res || []
  } catch (e) {}
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) return ElMessage.warning('账号和密码不能为空')
  loading.value = true
  try {
    const res: any = await request.post('/api/auth/login', loginForm)
    userStore.setTokens(res.accessToken, res.refreshToken)
    ElMessage.success('登录成功')
    
    if (userStore.role === 'ADMIN') router.push('/admin')
    else if (userStore.role === 'DOCTOR') router.push('/doctor')
    else router.push('/patient')
  } catch (error) {} finally {
    loading.value = false
  }
}

const submitRegister = async () => {
  loading.value = true
  try {
    if (registerRole.value === 'PATIENT') {
      await request.post('/api/auth/register/patient', regForm)
      ElMessage.success('患者注册成功，请登录！')
    } else {
      await request.post('/api/auth/register/doctor', regForm)
      ElMessage.success('医生入驻成功，请登录！')
    }
    loginForm.username = regForm.username
    loginForm.password = ''
    mode.value = 'login'
  } catch (error) {} finally {
    loading.value = false
  }
}

const quickLogin = (type: string) => {
  loginForm.username = type
  loginForm.password = '123456'
  handleLogin()
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
  overflow: auto;

  .action-card {
    width: 420px;
    border-radius: 12px;
    
    &.register-card {
      width: 550px; 
      margin: 40px 0;
    }

    .register-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .sys-title {
      text-align: center; margin: 0; color: #303133; font-weight: 600;
    }

    .full-btn {
      width: 100%; font-weight: bold; letter-spacing: 2px;
    }

    .card-footer {
      display: flex; justify-content: center; align-items: center; font-size: 14px; color: #606266; margin-bottom: 15px;
    }
    
    .mt-4 { margin-top: 15px; }
    
    :deep(.el-form-item) {
      margin-bottom: 15px;
    }
  }
}
</style>