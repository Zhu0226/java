<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2>📊 医院运营数据大盘</h2>
      <el-button type="primary" plain :loading="loading" @click="fetchStats">
        <el-icon style="margin-right: 5px"><Refresh /></el-icon> 实时刷新
      </el-button>
    </div>

    <div v-loading="loading">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="12" :md="6" class="mb-20">
          <el-card shadow="hover" class="stat-card blue-card">
            <div class="stat-icon"><el-icon><Avatar /></el-icon></div>
            <div class="stat-info">
              <div class="stat-title">全院在职医生</div>
              <div class="stat-value">{{ stats.activeDoctors }} <span class="unit">人</span></div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="12" :sm="12" :md="6" class="mb-20">
          <el-card shadow="hover" class="stat-card green-card">
            <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
            <div class="stat-info">
              <div class="stat-title">今日放出号源</div>
              <div class="stat-value">{{ stats.todayTotalTickets }} <span class="unit">个</span></div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="12" :sm="12" :md="6" class="mb-20">
          <el-card shadow="hover" class="stat-card orange-card">
            <div class="stat-icon"><el-icon><Checked /></el-icon></div>
            <div class="stat-info">
              <div class="stat-title">今日已被预约</div>
              <div class="stat-value">{{ stats.todayBookedTickets }} <span class="unit">个</span></div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="12" :sm="12" :md="6" class="mb-20">
          <el-card shadow="hover" class="stat-card purple-card">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-title">历史总营收</div>
              <div class="stat-value">¥ {{ stats.totalRevenue || '0.00' }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="mt-20">
        <template #header>
          <div class="card-header">
            <span>今日挂号率监控</span>
          </div>
        </template>
        <el-progress 
          :text-inside="true" 
          :stroke-width="24" 
          :percentage="bookingRate" 
          :status="bookingRate > 80 ? 'exception' : 'success'"
        />
        <p class="tip-text">当挂号率超过 80% 时，进度条将变为红色预警，建议管理员适时触发额外排班。</p>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Refresh, Avatar, Tickets, Checked, Money } from '@element-plus/icons-vue'
import request from '../../utils/request'

const loading = ref(false)
const stats = ref({
  activeDoctors: 0,
  todayTotalTickets: 0,
  todayBookedTickets: 0,
  totalRevenue: 0.00
})

// 计算挂号率
const bookingRate = computed(() => {
  if (stats.value.todayTotalTickets === 0) return 0
  return Math.round((stats.value.todayBookedTickets / stats.value.todayTotalTickets) * 100)
})

const fetchStats = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/admin/dashboard/stats')
    if (res) {
      stats.value = res
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .page-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }
  .mb-20 { margin-bottom: 20px; }
  .mt-20 { margin-top: 20px; }

  .stat-card {
    border-radius: 12px;
    color: white;
    border: none;
    display: flex;
    align-items: center;
    padding: 10px;
    
    :deep(.el-card__body) {
      display: flex;
      width: 100%;
      align-items: center;
      padding: 15px;
    }

    .stat-icon {
      font-size: 48px;
      margin-right: 20px;
      opacity: 0.8;
    }

    .stat-info {
      .stat-title { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
      .stat-value { font-size: 28px; font-weight: bold; }
      .unit { font-size: 14px; font-weight: normal; margin-left: 2px; }
    }
  }

  /* 精美的渐变色背景 */
  .blue-card { background: linear-gradient(135deg, #36D1DC 0%, #5B86E5 100%); }
  .green-card { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
  .orange-card { background: linear-gradient(135deg, #f12711 0%, #f5af19 100%); }
  .purple-card { background: linear-gradient(135deg, #8E2DE2 0%, #4A00E0 100%); }

  .tip-text { font-size: 13px; color: #909399; margin-top: 15px; }
}
</style>