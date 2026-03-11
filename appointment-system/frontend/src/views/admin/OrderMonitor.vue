<template>
  <div class="monitor-container">
    <div class="page-header">
      <h2>👁️ 全院订单实时监控</h2>
      <el-button type="primary" plain :loading="loading" @click="fetchOrders">
        <el-icon style="margin-right: 5px"><Refresh /></el-icon> 刷新流水
      </el-button>
    </div>

    <el-empty v-if="orderList.length === 0 && !loading" description="暂无订单记录" />

    <div v-else v-loading="loading">
      <div class="pc-view">
        <el-card shadow="never" class="table-card">
          <el-table :data="orderList" stripe style="width: 100%">
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="orderNo" label="订单号(Sharding)" min-width="220" />
            <el-table-column prop="patientId" label="患者ID" width="100" />
            <el-table-column label="就诊医生" width="120">
              <template #default="{ row }">
                <el-tag type="info" effect="plain">{{ row.doctorName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">
                <strong>¥ {{ row.amount }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="实时状态" width="120" fixed="right">
              <template #default="{ row }">
                <el-tag v-if="row.orderStatus === 0" type="info">待支付</el-tag>
                <el-tag v-else-if="row.orderStatus === 1" type="warning">待就诊</el-tag>
                <el-tag v-else-if="row.orderStatus === 2" type="success">已完成</el-tag>
                <el-tag v-else type="danger">已取消</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <div class="mobile-view">
        <el-card v-for="row in orderList" :key="row.orderNo" class="mobile-order-card" shadow="hover">
          <div class="card-header">
            <span class="time-text">{{ row.createTime }}</span>
            <el-tag v-if="row.orderStatus === 0" type="info" size="small">待支付</el-tag>
            <el-tag v-else-if="row.orderStatus === 1" type="warning" size="small">待就诊</el-tag>
            <el-tag v-else-if="row.orderStatus === 2" type="success" size="small">已完成</el-tag>
            <el-tag v-else type="danger" size="small">已取消</el-tag>
          </div>
          <div class="card-body">
            <p><strong>患者 ID：</strong> {{ row.patientId }}</p>
            <p><strong>就诊医生：</strong> {{ row.doctorName }}</p>
            <p><strong>挂号金额：</strong> ¥ {{ row.amount }}</p>
            <p class="order-no">单号：{{ row.orderNo }}</p>
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

const orderList = ref<any[]>([])
const loading = ref(false)

const fetchOrders = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/admin/orders/latest')
    // 简单处理一下时间字符串，去掉末尾的秒数小数
    orderList.value = (res || []).map((item: any) => ({
      ...item,
      createTime: item.createTime ? item.createTime.replace('T', ' ').substring(0, 19) : ''
    }))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.monitor-container {
  .page-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }
  .pc-view { display: block; .table-card { border-radius: 8px; } }
  .mobile-view { display: none; }

  @media screen and (max-width: 768px) {
    .pc-view { display: none; }
    .mobile-view { 
      display: block; 
      .mobile-order-card {
        margin-bottom: 15px; border-radius: 10px;
        .card-header {
          display: flex; justify-content: space-between; align-items: center;
          border-bottom: 1px solid #ebeef5; padding-bottom: 10px; margin-bottom: 10px;
          .time-text { font-size: 14px; font-weight: bold; color: #606266; }
        }
        .card-body {
          p { margin: 5px 0; color: #606266; font-size: 14px; }
          .order-no { font-size: 12px; color: #c0c4cc; margin-top: 10px; word-break: break-all; }
        }
      }
    }
  }
}
</style>