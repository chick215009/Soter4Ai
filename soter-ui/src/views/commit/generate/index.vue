<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <h3>生成记录</h3>
      </el-col>

      <el-col :span="18">
        <el-card shadow="hover">
          <span>本地项目地址</span>
          <el-input
            v-model="queryParams.localProjectPath"
            placeholder="请输入本地项目地址"
            clearable
            size="small"
            style="width: 500px; margin-left: 20px"
          >
          </el-input>
          <el-button
            size="small"
            @click="handleGenerate"
            type="primary"
            style="margin-left: 20px"
          >
            生成代码提交说明
          </el-button>

        </el-card>
        <el-col :span="12">
          <el-card style="height:300px; margin-top: 20px">
            <h3>视图1</h3>
          </el-card>
          <el-card style="height:300px; margin-top: 20px">
            <h3>视图2</h3>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-input
            style="margin-top: 20px"
            type="textarea"
            :rows="30"
            v-model="commit_message"
          >
          </el-input>
        </el-col>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import {generateCommit} from "@/api/commit/generate";

export default {
  name: "Generate",
  data() {
    return {
      queryParams: {
        localProjectPath: null
      },
      commit_message: null
    }
  },
  methods: {
    handleGenerate() {
      generateCommit(this.queryParams).then(response=>{
      })
    }
  }
}
</script>
