<template>
  <div class="workbench-container">
    <div class="page-header">
      <h2>🩺 今日接诊队列</h2>
      <el-button type="primary" :loading="loading" @click="fetchTodayOrders">
        <el-icon style="margin-right: 5px"><Refresh /></el-icon> 刷新队列
      </el-button>
    </div>

    <el-empty v-if="orderList.length === 0 && !loading" description="今日暂无挂号患者，喝杯茶休息一下吧~" />

    <div v-else v-loading="loading">
      <div class="pc-view">
        <el-card shadow="never" class="table-card">
          <el-table :data="orderList" stripe style="width: 100%">
            <el-table-column prop="ticketNo" label="排号" width="80" align="center">
              <template #default="{ row }">
                <div class="ticket-badge">{{ row.ticketNo }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="挂号单号" width="220" />
            <el-table-column prop="patientId" label="患者ID" width="120" />
            <el-table-column label="预约时段" width="180">
              <template #default="{ row }">
                <el-icon><Clock /></el-icon> {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.orderStatus === 1 ? 'warning' : 'success'" effect="light">
                  {{ row.orderStatus === 1 ? '等待就诊' : '就诊完毕' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.orderStatus === 1" 
                  type="success" 
                  @click="completeConsultation(row.orderNo)"
                >
                  开始接诊并完成
                </el-button>
                <span v-else class="text-muted">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <div class="mobile-view">
        <el-card 
          v-for="row in orderList" 
          :key="row.orderNo" 
          class="mobile-patient-card" 
          shadow="hover"
        >
          <div class="card-header">
            <span class="ticket-number">第 {{ row.ticketNo }} 号</span>
            <el-tag :type="row.orderStatus === 1 ? 'warning' : 'success'" size="small">
              {{ row.orderStatus === 1 ? '待就诊' : '已完成' }}
            </el-tag>
          </div>
          <div class="card-body">
            <p><strong>患者 ID：</strong> {{ row.patientId }}</p>
            <p><strong>时 段：</strong> {{ row.startTime }} - {{ row.endTime }}</p>
            <p class="order-no">单号：{{ row.orderNo }}</p>
          </div>
          <div class="card-footer" v-if="row.orderStatus === 1">
            <el-button type="success" size="large" class="full-btn" @click="completeConsultation(row.orderNo)">
              ✅ 完成就诊
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh, Clock } from '@element-plus/icons-vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const orderList = ref<any[]>([])
const loading = ref(false)

const fetchTodayOrders = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/doctor/workbench/today-list')
    orderList.value = res || []
  } finally {
    loading.value = false
  }
}

const completeConsultation = async (orderNo: string) => {
  ElMessageBox.confirm('确认该患者已就诊完毕吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.post('/api/doctor/workbench/complete', { orderNo })
      ElMessage.success('接诊状态已更新')
      fetchTodayOrders()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  fetchTodayOrders()
})
</script>

<style scoped lang="scss">
.workbench-container {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }

  .text-muted { color: #909399; font-size: 14px; }

  /* PC端样式 */
  .pc-view {
    display: block;
    .table-card { border-radius: 8px; }
    .ticket-badge {
      background-color: #409eff; color: white;
      width: 30px; height: 30px; line-height: 30px;
      border-radius: 50%; font-weight: bold; margin: 0 auto;
    }
  }

  /* 移动端隐藏 */
  .mobile-view { display: none; }

  /* ========== 响应式魔法：当屏幕小于 768px 时 ========== */
  @media screen and (max-width: 768px) {
    .pc-view { display: none; } /* 隐藏表格 */
    .mobile-view { 
      display: block; /* 显示卡片 */
      
      .mobile-patient-card {
        margin-bottom: 15px;
        border-radius: 10px;
        
        .card-header {
          display: flex; justify-content: space-between; align-items: center;
          border-bottom: 1px solid #ebeef5; padding-bottom: 10px; margin-bottom: 10px;
          .ticket-number { font-size: 18px; font-weight: bold; color: #409eff; }
        }

        .card-body {
          p { margin: 5px 0; color: #606266; font-size: 14px; }
          .order-no { font-size: 12px; color: #c0c4cc; margin-top: 10px; }
        }

        .card-footer {
          margin-top: 15px;
          .full-btn { width: 100%; border-radius: 8px; }
        }
      }
    }
  }
}
</style>