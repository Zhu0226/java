<template>
  <div class="schedule-container">
    <div class="page-header">
      <h2>📅 我的排班 (近7天)</h2>
      <el-button type="primary" plain :loading="loading" @click="fetchSchedule">
        <el-icon style="margin-right: 5px"><Refresh /></el-icon> 刷新排班
      </el-button>
    </div>

    <el-empty v-if="scheduleList.length === 0 && !loading" description="近7天暂无排班记录" />

    <div v-else v-loading="loading">
      <div class="pc-view">
        <el-card shadow="never" class="table-card">
          <el-table :data="scheduleList" stripe style="width: 100%">
            <el-table-column prop="workDate" label="出诊日期" width="180">
              <template #default="{ row }">
                <strong>{{ row.workDate }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="shiftType" label="班次" width="120">
              <template #default="{ row }">
                <el-tag :type="row.shiftType === 1 ? 'primary' : 'warning'" effect="light">
                  {{ row.shiftType === 1 ? '上午 (08:00-12:00)' : '下午 (14:00-18:00)' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="号源售卖情况 (余量/总量)" min-width="200">
              <template #default="{ row }">
                <el-progress 
                  :text-inside="true" 
                  :stroke-width="20" 
                  :percentage="Math.round(((row.totalNum - row.availableNum) / row.totalNum) * 100)" 
                  :status="row.availableNum === 0 ? 'exception' : 'success'"
                >
                  <span>余 {{ row.availableNum }} / 共 {{ row.totalNum }}</span>
                </el-progress>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <div class="mobile-view">
        <el-card 
          v-for="row in scheduleList" 
          :key="row.id" 
          class="mobile-schedule-card" 
          shadow="hover"
        >
          <div class="card-header">
            <span class="date-text">{{ row.workDate }}</span>
            <el-tag :type="row.shiftType === 1 ? 'primary' : 'warning'" size="small" round>
              {{ row.shiftType === 1 ? '上午' : '下午' }}
            </el-tag>
          </div>
          <div class="card-body">
            <p><strong>号源情况：</strong> 剩余 {{ row.availableNum }} / 总量 {{ row.totalNum }}</p>
            <el-progress 
              :percentage="Math.round(((row.totalNum - row.availableNum) / row.totalNum) * 100)" 
              :show-text="false"
              :status="row.availableNum === 0 ? 'exception' : 'success'"
              style="margin-top: 10px;"
            />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import request from '../../utils/request'

const scheduleList = ref<any[]>([])
const loading = ref(false)

const fetchSchedule = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/doctor/workbench/schedule-list')
    scheduleList.value = res || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSchedule()
})
</script>

<style scoped lang="scss">
.schedule-container {
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
      
      .mobile-schedule-card {
        margin-bottom: 15px;
        border-radius: 10px;
        
        .card-header {
          display: flex; justify-content: space-between; align-items: center;
          border-bottom: 1px solid #ebeef5; padding-bottom: 10px; margin-bottom: 10px;
          .date-text { font-size: 16px; font-weight: bold; color: #303133; }
        }

        .card-body {
          p { margin: 5px 0; color: #606266; font-size: 14px; }
        }
      }
    }
  }
}
</style>