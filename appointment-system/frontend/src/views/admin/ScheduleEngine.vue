<template>
  <div class="admin-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>👨‍💻 系统管理员控制台</h2>
          <el-tag type="danger">ADMIN</el-tag>
        </div>
      </template>
      
      <h3>智能排班引擎跑批</h3>
      <p style="color: #666; margin-bottom: 20px;">
        手动触发全院各科室医生指定日期的排班与号源池生成。
      </p>

      <div class="action-bar">
        <el-date-picker
          v-model="targetDate"
          type="date"
          placeholder="选择目标排班日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          size="large"
        />
        <el-button 
          type="primary" 
          size="large" 
          @click="triggerBatch" 
          :loading="loading"
          style="margin-left: 15px;"
        >
          🚀 触发全院跑批
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const targetDate = ref('')
const loading = ref(false)

const triggerBatch = async () => {
  if (!targetDate.value) {
    ElMessage.warning('请先选择目标日期')
    return
  }
  
  loading.value = true
  try {
    // 对接后端管理员接口
    await request.post(`/api/admin/schedule/trigger-generate?dateStr=${targetDate.value}`)
    ElMessage.success('🚀 排班跑批任务执行成功！Redis预热已完成。')
  } catch (error) {
    // 错误由 axios 拦截器统一个处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-dashboard { padding: 40px; background-color: #f2f3f5; min-height: 100vh; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.action-bar { display: flex; align-items: center; padding: 20px; background: #f8f9fa; border-radius: 8px; }
</style>