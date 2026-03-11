<template>
  <div class="history-container">
    <div class="page-header">
      <h2>📚 历史接诊记录</h2>
      <el-button type="primary" plain :loading="loading" @click="fetchHistory">
        <el-icon style="margin-right: 5px"><Refresh /></el-icon> 刷新记录
      </el-button>
    </div>

    <el-empty v-if="historyList.length === 0 && !loading" description="暂无历史接诊记录" />

    <div v-else v-loading="loading">
      <div class="pc-view">
        <el-card shadow="never" class="table-card">
          <el-table :data="historyList" stripe style="width: 100%">
            <el-table-column prop="workDate" label="就诊日期" width="120" />
            <el-table-column label="就诊时段" width="160">
              <template #default="{ row }">
                {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="ticketNo" label="排号" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="info" effect="plain">{{ row.ticketNo }} 号</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="patientId" label="患者ID" width="120" />
            <el-table-column prop="orderNo" label="挂号单号" min-width="200" />
            <el-table-column prop="orderStatus" label="状态" fixed="right" width="100" align="center">
              <template #default>
                <el-tag type="success">已完成</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <div class="mobile-view">
        <el-card 
          v-for="row in historyList" 
          :key="row.orderNo" 
          class="mobile-history-card" 
          shadow="hover"
        >
          <div class="card-header">
            <span class="date-text"><el-icon><Calendar /></el-icon> {{ row.workDate }}</span>
            <el-tag type="success" size="small" effect="dark">已完成</el-tag>
          </div>
          <div class="card-body">
            <p><strong>排号/时段：</strong> 第 {{ row.ticketNo }} 号 ({{ row.startTime }} - {{ row.endTime }})</p>
            <p><strong>患者 ID：</strong> {{ row.patientId }}</p>
            <p class="order-no">单号：{{ row.orderNo }}</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh, Calendar } from '@element-plus/icons-vue'
import request from '../../utils/request'

const historyList = ref<any[]>([])
const loading = ref(false)

const fetchHistory = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/doctor/workbench/history-list')
    historyList.value = res || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped lang="scss">
.history-container {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }

  /* PC端样式 */
  .pc-view {
    display: block;
    .table-card { border-radius: 8px; }
  }

  /* 移动端隐藏 */
  .mobile-view { display: none; }

  /* 响应式魔法 */
  @media screen and (max-width: 768px) {
    .pc-view { display: none; }
    .mobile-view { 
      display: block; 
      
      .mobile-history-card {
        margin-bottom: 15px;
        border-radius: 10px;
        border-left: 4px solid #67c23a; /* 增加一点历史记录特有的视觉标识 */
        
        .card-header {
          display: flex; justify-content: space-between; align-items: center;
          border-bottom: 1px solid #ebeef5; padding-bottom: 10px; margin-bottom: 10px;
          .date-text { font-size: 15px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 5px; }
        }

        .card-body {
          p { margin: 5px 0; color: #606266; font-size: 14px; }
          .order-no { font-size: 12px; color: #c0c4cc; margin-top: 10px; }
        }
      }
    }
  }
}
</style>