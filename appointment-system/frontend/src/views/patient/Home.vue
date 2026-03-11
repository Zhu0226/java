<template>
  <div class="home-container">
    <van-notice-bar
      left-icon="volume-o"
      text="欢迎使用智慧医院预约挂号系统。系统已开启高并发保护，请勿频繁刷新。"
    />

    <div class="section">
      <div class="section-title">第一步：选择就诊科室</div>
      <van-cell-group inset>
        <van-cell 
          v-for="dept in deptList" 
          :key="dept.id" 
          :title="dept.deptName" 
          is-link 
          :class="{ 'active-cell': selectedDeptId === dept.id }"
          @click="selectDept(dept.id)"
        />
      </van-cell-group>
    </div>

    <div v-if="selectedDeptId" class="section">
      <div class="section-title">第二步：选择出诊医生/时段</div>
      <van-empty v-if="scheduleList.length === 0" description="该科室暂无排班数据" />
      
      <van-cell-group inset v-else>
        <div 
          v-for="item in scheduleList" 
          :key="item.scheduleId" 
          class="schedule-item"
          @click="selectedSchedule = item"
          :class="{ 'active-schedule': selectedSchedule?.scheduleId === item.scheduleId }"
        >
          <div class="doc-info">
            <span class="doc-name">{{ item.doctorName }}</span>
            <van-tag type="primary" plain>{{ item.title }}</van-tag>
          </div>
          <div class="time-info">
            <span>{{ item.workDate }} ({{ item.shiftType === 1 ? '上午' : '下午' }})</span>
            <span class="fee">￥{{ item.consultationFee }}</span>
          </div>
          <div class="stock-info">
            剩余号源：<span :class="item.availableNum > 0 ? 'count-green' : 'count-red'">{{ item.availableNum }}</span>
          </div>
        </div>
      </van-cell-group>
    </div>

    <div v-if="selectedSchedule" class="action-bar">
      <div class="price-info">
        挂号费：<span class="price">￥{{ selectedSchedule.consultationFee }}</span>
      </div>
      <van-button 
        type="primary" 
        round 
        class="order-btn" 
        :loading="ordering"
        :disabled="selectedSchedule.availableNum <= 0"
        @click="handleCreateOrder"
      >
        立即挂号
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { showDialog, showFailToast } from 'vant'

const deptList = ref<any[]>([])
const scheduleList = ref<any[]>([])
const selectedDeptId = ref<number | null>(null)
const selectedSchedule = ref<any>(null)
const ordering = ref(false)

// 获取科室列表
const fetchDepts = async () => {
  try {
    // 👈 注意这里：路径改成了 /api/dept/list，去掉了 /patient
    const res: any = await request.get('/api/dept/list') 
    deptList.value = res || []
  } catch (e) {}
}

const selectDept = async (id: number) => {
  selectedDeptId.value = id
  selectedSchedule.value = null
  try {
    // 【核心修复】：改为 ?deptId=xxx 格式，匹配后端的 @RequestParam
    const res: any = await request.get(`/api/schedule/list?deptId=${id}`)
    scheduleList.value = res || []
  } catch (e) {}
}

// 提交挂号请求
const handleCreateOrder = async () => {
  if (!selectedSchedule.value) return
  
  ordering.value = true
  try {
    const res: any = await request.post('/api/patient/order/create', {
      scheduleId: selectedSchedule.value.scheduleId
    })
showDialog({
      title: '挂号成功',
      message: `您的预约单号为：\n${res.orderNo}\n请在“我的订单”中查看`,
    })
    // 刷新当前排班数据
    selectDept(selectedDeptId.value!)
  } catch (e: any) {
    showFailToast(e.message || '抢号失败，请重试')
  } finally {
    ordering.value = false
  }
}

onMounted(() => {
  fetchDepts()
})
</script>

<style scoped lang="scss">
.home-container {
  padding-bottom: 100px; /* 为下方悬浮按钮留出空间 */

  .section {
    margin-top: 15px;
    .section-title {
      font-size: 14px;
      color: #909399;
      padding: 0 16px 8px;
    }
  }

  .active-cell {
    background-color: #ecf5ff;
    color: #409eff;
  }

  .schedule-item {
    padding: 15px;
    border-bottom: 1px solid #f2f6fc;
    background: white;
    transition: all 0.2s;
    
    .doc-info {
      display: flex;
      align-items: center;
      gap: 10px;
      .doc-name { font-size: 16px; font-weight: bold; }
    }
    
    .time-info {
      margin-top: 8px;
      font-size: 13px;
      color: #606266;
      display: flex;
      justify-content: space-between;
      .fee { color: #f56c6c; font-weight: bold; }
    }

    .stock-info {
      margin-top: 5px;
      font-size: 12px;
      color: #909399;
      .count-green { color: #67c23a; font-weight: bold; }
      .count-red { color: #f56c6c; }
    }
  }

  .active-schedule {
    border: 1px solid #1989fa;
    background-color: #f0f7ff;
  }

  /* 底部悬浮操作条 */
  .action-bar {
    position: fixed;
    bottom: 50px; /* 在 Tabbar 之上 */
    left: 0;
    right: 0;
    height: 60px;
    background: white;
    box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    z-index: 99;

    .price-info {
      font-size: 14px;
      .price { color: #f56c6c; font-size: 18px; font-weight: bold; }
    }
    .order-btn {
      width: 120px;
    }
  }
}
</style>