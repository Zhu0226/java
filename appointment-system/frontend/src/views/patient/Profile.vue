<template>
  <div class="profile-container">
    <div class="header-card">
      <div class="user-info">
        <div class="avatar-placeholder">
          <span class="avatar-text">{{ profile.realName ? profile.realName.substring(0,1) : '用' }}</span>
        </div>
        <div class="info-detail">
          <div class="name-line">
            <span class="name">{{ profile.realName || '未命名' }}</span>
            <span class="tag-gender" v-if="profile.gender === 1">男</span>
            <span class="tag-gender girl" v-else-if="profile.gender === 2">女</span>
            <span class="tag-age">{{ profile.age }}岁</span>
          </div>
          <div class="privacy-line">
            📞 手机号：{{ profile.phone || '暂无数据' }}
          </div>
          <div class="privacy-line">
            💳 身份证：{{ profile.idCard || '暂无数据' }}
          </div>
        </div>
      </div>
    </div>

    <div class="grid-card">
      <van-grid :column-num="3" :border="false">
        <van-grid-item icon="balance-list-o" text="待支付" :badge="profile.pendingPayCount || ''" @click="goToOrder" />
        <van-grid-item icon="clock-o" text="待就诊" :badge="profile.pendingVisitCount || ''" @click="goToOrder" />
        <van-grid-item icon="passed" text="已完成" :badge="profile.completedCount || ''" @click="goToOrder" />
      </van-grid>
    </div>

    <div class="menu-list">
      <van-cell-group inset>
        <van-cell title="我的挂号单" icon="orders-o" is-link @click="goToOrder" />
        <van-cell title="就诊人管理" icon="friends-o" is-link />
        <van-cell title="关于系统" icon="info-o" is-link value="v1.0.0" />
      </van-cell-group>
    </div>

    <div class="logout-btn">
      <van-button type="danger" block round plain @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import request from '../../utils/request'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

// 初始化时给上默认值，防止由于响应过慢导致的短暂 undefined 渲染报错
const profile = ref({
  realName: '加载中...',
  phone: '',
  idCard: '',
  gender: 0,
  age: 0,
  pendingPayCount: 0,
  pendingVisitCount: 0,
  completedCount: 0
})

onMounted(async () => {
  try {
    const res: any = await request.get('/api/patient/profile/info')
    if (res) {
      profile.value = res
    }
  } catch (error) {
    console.error("获取个人信息失败", error)
  }
})

const goToOrder = () => {
  router.push('/patient/orders')
}

const handleLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出当前账号吗？',
  }).then(async () => {
    try {
      await request.post('/api/auth/logout')
    } catch (e) {
    } finally {
      // 这里调用的是上一轮修好的 clearAuth
      userStore.clearAuth()
      showToast('已安全退出')
      router.replace('/login')
    }
  }).catch(() => {});
}
</script>

<style scoped lang="scss">
.profile-container {
  min-height: calc(100vh - 50px);
  background: #f7f8fa;
  padding-bottom: 20px;

  .header-card {
    background: linear-gradient(135deg, #1989fa 0%, #4facfe 100%);
    padding: 40px 20px 30px;
    border-radius: 0 0 24px 24px;
    color: white;

    .user-info {
      display: flex;
      align-items: center;

      .avatar-placeholder {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background-color: rgba(255,255,255,0.2);
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 15px;
        border: 2px solid rgba(255,255,255,0.4);
        
        .avatar-text {
          font-size: 24px;
          font-weight: bold;
        }
      }

      .info-detail {
        flex: 1;

        .name-line {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          .name {
            font-size: 22px;
            font-weight: bold;
            margin-right: 10px;
          }
          
          .tag-gender, .tag-age {
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 12px;
            margin-right: 6px;
            background: rgba(255,255,255,0.2);
            border: 1px solid rgba(255,255,255,0.5);
          }
          
          .tag-gender.girl {
            background: rgba(255, 105, 180, 0.4);
            border-color: rgba(255, 105, 180, 0.8);
          }
        }

        .privacy-line {
          font-size: 13px;
          opacity: 0.9;
          margin-top: 4px;
          letter-spacing: 1px;
        }
      }
    }
  }

  .grid-card {
    margin: -15px 15px 15px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  }

  .menu-list {
    margin-bottom: 20px;
  }

  .logout-btn {
    padding: 0 15px;
    margin-top: 30px;
  }
}
</style>