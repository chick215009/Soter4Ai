<template>
  <div>
    <div
    >
      <el-button
        @click=handleShowHistory
        size="small"
        round
      >
        {{ showHistoryLabel }}
      </el-button>
    </div>

    <div
      v-show="showHistory"
    >
      <div>
        <el-select
          v-model="value"
          placeholder="请选择项目地址"
          @change="handleSelectProject"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <el-button
        size="small"
        style="margin-left: 20px"
        @click="handleAnalyzeHistory"
        >
          历史分析
        </el-button>
      </div>
      <div
        title="历史记录"
      >
        <el-table
          :data="historyList"
          @row-click="jumpHistoryDetail"
        >
          <el-table-column label="生成时间" align="center" prop="generatedDate" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.generatedDate, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="地址" align="center" prop="localProjectPath" width="200"/>
        </el-table>
      </div>
    </div>


  </div>


</template>

<script>

import {getHistoryList, getHistoryListByProject, getProjectList} from "@/api/commit/generate";
export default {
  name: "index",
  data() {
    return {
      // drawer: false,
      showHistory: false,
      showHistoryLabel: "显示历史记录",
      historyList: [],
      options: [],
      value: '',
      queryParams: {
        path: ''
      }
    };
  },
  methods: {
    handleShowHistory() {
      if (this.showHistory) {
        this.showHistoryLabel = "显示历史记录"
        // this.initHistoryList();
      } else {
        this.initProjectOptions();
        this.showHistoryLabel = "隐藏历史记录"
      }
      this.showHistory = !this.showHistory;
    },
    jumpHistoryDetail(row) {
      this.$router.push({
        name: 'Generate',
        params: {
          jump_row: row
        },
      });
    },
    initHistoryList() {
      getHistoryList().then(response => {
        this.historyList = response.rows;
      });
    },
    handleSelectProject(val) {
      this.queryParams.path = val;
      getHistoryListByProject(this.queryParams).then(response => {
        this.historyList = response.rows;
      })
    },
    initProjectOptions() {
      getProjectList().then(response => {
        // console.log(response.paths)
        this.options =  response.paths;
      })
    },
    handleAnalyzeHistory() {
      this.$router.push({
        name: 'Current',
        params: {
          project_path: this.value
        }
      });
    }
  }
}
</script>

