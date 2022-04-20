<template>
  <div class="app-container"
       v-loading="isShowLoading"
  >
    <el-row>
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>请输入GitHub项目的</span>
            <span style="color: #1a7bb9">仓库名</span>
            <el-switch
              v-model="isURL"
              active-color="#13ce66"
              inactive-color="#1a7bb9"
            >
            </el-switch>
            <span style="color: #13ce66">URL</span>

            <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-check">载入项目</el-button>
            <br>

            <div
              style="margin-top: 5px;margin-bottom: 5px"
              v-show="isURL"
            >
              <el-row>
                <span > URL </span>
                <el-input placeholder="例如：https://github.com/author/repo" v-model="repoURL" style="width: 400px">
                </el-input>
              </el-row >
            </div>


            <div
              v-show="!isURL"
              style="margin-top: 5px;margin-bottom: 5px"
            >
              <el-row>
                <el-form :model="query">
                  <el-row  >
                    <span> https://github.com/ </span>
                    <el-input placeholder="请输入GitHub项目作者" v-model="query.username" style="width: 175px">
                    </el-input>
                    <span> / </span>
                    <el-input placeholder="请输入GitHub项目仓库名" v-model="query.repoName" style="width: 175px">
                    </el-input>
                  </el-row>
                </el-form>
              </el-row>
            </div>


          </div>
          <!--          <div v-for="o in 4" :key="o" class="text item">-->
          <!--            {{'列表内容 ' + o }}-->
          <!--          </div>-->
          <el-descriptions :column="1">
<!--            <el-descriptions-item label="项目名">RuoYi</el-descriptions-item>-->
<!--            <el-descriptions-item label="GitHub URL地址">https://github.com/yangzongzhuan/RuoYi</el-descriptions-item>-->
            <el-descriptions-item label="最新版本">v4.7.3</el-descriptions-item>
            <el-descriptions-item label="最后一次提交">2022-03-01</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <div style="margin-left: 20px">
          <el-row>
            <span>{{preTag}} ~ </span>
            <el-select
              v-model="value1"
              placeholder="查看此版本与相邻上一个版本之间的历史提交记录"
              style="width: 400px"
              @visible-change="displayTags($event)"
              @change="getCommitByTag"
            >
              <el-option
                v-for="item in options"
                :key="item.label"
                :label="item.label"
                :value="item.label">
              </el-option>
            </el-select>
          </el-row>

          <el-button
            size="smail"
            @click="getRecent"
            type="primary"
            icon="el-icon-search"
            style="margin-top: 20px"
          >查看提交记录
          </el-button>
        </div>
      </el-col>
    </el-row>
<!--    <h3>请填写以下GitHub项目信息</h3>-->
<!--    <el-row style="margin-bottom: 20px">-->
<!--      <el-form :model="query">-->
<!--        <el-row  >-->
<!--          <template> https://github.com/ </template>-->
<!--          <el-input placeholder="请输入GitHub项目作者" v-model="query.username" style="width: 200px">-->
<!--          </el-input>-->
<!--          <template style="background-color: #C9C9C9"> / </template>-->
<!--          <el-input placeholder="请输入GitHub项目仓库名" v-model="query.repoName" style="width: 200px">-->
<!--          </el-input>-->

<!--          <el-button-->
<!--            size="smail"-->
<!--            @click="getRecent"-->
<!--            style="margin-left: 30px"-->
<!--            type="primary"-->
<!--            icon="el-icon-search"-->
<!--          >查看提交记录-->
<!--          </el-button>-->
<!--        </el-row>-->
<!--      </el-form>-->

<!--    </el-row>-->
<!--    <el-form :model="query">-->
<!--      <el-form-item label="作者" prop="username">-->
<!--        <el-input-->
<!--          v-model="query.username"-->
<!--          placeholder="请输入作者"-->
<!--          clearable-->
<!--          size="small"-->
<!--        />-->
<!--      </el-form-item>-->
<!--      <el-form-item label="仓库名" prop="repoName">-->
<!--        <el-input-->
<!--          v-model="query.repoName"-->
<!--          placeholder="请输入仓库名"-->
<!--          clearable-->
<!--          size="small"-->
<!--        />-->
<!--      </el-form-item>-->
<!--    </el-form>-->

<!--    <el-row>-->
<!--      <el-select-->
<!--        v-model="value1"-->
<!--        placeholder="查看此版本与相邻上一个版本之间的历史提交记录"-->
<!--        style="width: 400px"-->
<!--        @visible-change="displayTags($event)"-->
<!--        @change="getCommitByTag"-->
<!--      >-->
<!--        <el-option-->
<!--          v-for="item in options"-->
<!--          :key="item.label"-->
<!--          :label="item.label"-->
<!--          :value="item.label">-->
<!--        </el-option>-->
<!--      </el-select>-->
<!--    </el-row>-->


    <el-table
      :data="tableData.filter(data => !search || data.message.toLowerCase().includes(search.toLowerCase()))"
      style="width: 100%; margin-top: 10px"
      :default-sort = "{prop: 'date', order: 'descending'}"
      @row-click="analyzeCommit"
    >
      <el-table-column
        prop="sha"
        label="commit SHA"
        width="200"
      >
      </el-table-column>
      <el-table-column
        prop="message"
        label="提交信息"
        :sortable="true"
        :sort-method="sortMessage"
        width="300">
      </el-table-column>
      <el-table-column
        prop="date"
        label="提交日期"
        sortable>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.date, '{y}-{m}-{d}') }}</span>
        </template>
        <!--      :formatter="formatter"-->
        <!--    >-->
        <!--      :formatter="formatter"-->
      </el-table-column>
      <el-table-column
        align="right">
        <template slot="header" slot-scope="scope">
          <el-input
            v-model="search"
            size="mini"
            placeholder="对提交信息进行关键字搜索"/>
        </template>
        <template slot-scope="scope">
          <el-button
            size="mini"
            @click="handleJump(scope.$index, scope.row)">提交页面</el-button>
<!--          <el-button-->
<!--            size="mini"-->
<!--            type="danger"-->
<!--            @click="handleDelete(scope.$index, scope.row)">Delete</el-button>-->
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style>
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  width: 100%;
}
</style>

<script>
import {getRecentCommit, get2CommitDiff, analyzeDisplayProjectTags, getCommitInTag} from '@/api/github/commit'
export default {
  name: "index",
  data() {
    return {
      preTag: '-',
      tableData: [],
      repoURL: null,
      isURL: true,
      value1:[],
      options: [],
      search: '',
      query: {
        username:'',
        repoName:'',
        tag:''
      },
      isShowLoading:false,
      commitData: {
        username:'',
        repoName: '',
        preCommit:'',
        nextCommit:''
      }
    }
  },
  methods: {
    formatter(row, column) {
      return row.address;
    },
    handleJump(index, row) {
      var html_url = "https://github.com/" + this.query.username + "/" + this.query.repoName + "/commit/" + row.sha;
      window.open(html_url, '_blank');
    },
    displayTags(visible) {
      if (visible) {
        this.isShowLoading = true;
        analyzeDisplayProjectTags(this.query).then(response => {
          console.log(response);
          this.options = response.data;
          this.isShowLoading = false;
        })
      }
    },
    getRecent() {
      this.isShowLoading = true;
      getRecentCommit(this.query).then(response => {
        this.tableData = response.rows;
        this.isShowLoading = false;
      });
    },
    getCommitByTag() {
      this.isShowLoading = true;
      this.query.tag = this.value1;
      getCommitInTag(this.query).then(response => {
        this.tableData = response.rows;
        this.isShowLoading = false;
        for (var i = 0; i < this.options.length; i++) {
          if (this.options[i].label == this.value1) {
            this.preTag = this.options[i + 1].label;
          }
        }
      })
    },
    sortMessage(a, b) {
      if (a.message == null || a.message == "" || a.message.length < b.message.length) {
        return 1;
      } else {
        return -1;
      }
    },
    analyzeCommit(row) {
      var index = this.tableData.indexOf(row);
      var nextCommit = row.sha;
      var preCommit = this.tableData[index + 1].sha;
      this.commitData.preCommit = preCommit;
      this.commitData.nextCommit = nextCommit;
      this.commitData.username = this.query.username;
      this.commitData.repoName = this.query.repoName;
      var isCommitAnalyze = true;
      window.open("http://localhost:1024/code/generate?" +
        "isCommitAnalyze=" + isCommitAnalyze +
      "&preCommit=" + this.commitData.preCommit +
      "&nextCommit=" + this.commitData.nextCommit +
      "&username=" + this.commitData.username +
      "&repoName=" + this.commitData.repoName);
      // window.open("http://localhost:1024/commit/generate?commitData=" + this.commitData, "_blank")
      // this.$router.push({
      //   name: 'Generate',
      //   params: {
      //     commitData: this.commitData
      //   }
      // })
      // get2CommitDiff(this.commitData).then(response => {
      //   console.log(response);
      // });
    }
  }
}
</script>

<style scoped>

</style>
