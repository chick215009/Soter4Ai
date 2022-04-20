<template>
  <div class="app-container"
       v-loading="isShowLoading"
  >
    <el-row
      :gutter="20"
    >
<!--      <el-col :span="6">-->
<!--        <h3>生成记录</h3>-->
<!--        <el-table-->
<!--          :data="historyList"-->
<!--          @row-click = "historyDetail"-->
<!--          v-loading="historyLoading"-->
<!--        >-->
<!--          <el-table-column label="生成时间" align="center" prop="generatedDate"  width="180">-->
<!--            <template slot-scope="scope">-->
<!--              <span>{{ parseTime(scope.row.generatedDate, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>-->
<!--            </template>-->
<!--          </el-table-column>-->
<!--          <el-table-column label="地址" align="center" prop="localProjectPath" />-->
<!--        </el-table>-->

<!--      </el-col>-->

      <el-col>
<!--        <el-card shadow="hover">-->
<!--          <span>本地项目地址</span>-->
<!--          <el-input-->
<!--            v-model="queryParams.localProjectPath"-->
<!--            placeholder="请输入本地项目地址"-->
<!--            clearable-->
<!--            size="small"-->
<!--            style="width: 500px; margin-left: 20px"-->
<!--          >-->
<!--          </el-input>-->
<!--          <el-button-->
<!--            size="small"-->
<!--            @click="handleGenerate"-->
<!--            type="primary"-->
<!--            style="margin-left: 20px"-->
<!--          >-->
<!--            生成代码提交说明-->
<!--          </el-button>-->

<!--        </el-card>-->
        <div>
          <el-col :span="18">
            <div v-html="commit_message"></div>
          </el-col>
          <el-col :span="6">
            <el-card style="height:400px; margin-top: 20px">
              <el-descriptions title="变化统计" :column="1">
                <el-descriptions-item label="提交类型">{{commit_stereotype}}</el-descriptions-item>
                <el-descriptions-item label="总文件数">{{file_num}}</el-descriptions-item>
                <el-descriptions-item label="新增文件">{{add_num}}</el-descriptions-item>
                <el-descriptions-item label="删除文件">{{remove_num}}</el-descriptions-item>
                <el-descriptions-item label="修改文件">{{changed_num}}</el-descriptions-item>
              </el-descriptions>
              <font size="2px" style="color: grey">在新增和修改的文件中，变化或者新增的方法元素模式占比如下</font>
              <div class="pie">
                <div id="pie1">
                  <!-- 为 ECharts 准备一个具备大小（宽高）的 DOM -->
                  <div id="main1" style="float:left;width: 100%; height: 100%"></div>
                </div>
              </div>
            </el-card>
<!--            <el-card style="height:300px; margin-top: 20px">-->
<!--              <span>method stereotype 统计</span>-->
<!--              <div class="pie">-->
<!--                <div id="pie1">-->
<!--                  &lt;!&ndash; 为 ECharts 准备一个具备大小（宽高）的 DOM &ndash;&gt;-->
<!--                  <div id="main1" style="float:left;width:500px;height: 300px"></div>-->
<!--                </div>-->
<!--              </div>-->
<!--            </el-card>-->
          </el-col>

        </div>

      </el-col>
    </el-row>

  </div>
</template>

<script>
import {generateCommit, getStatistics, getHistoryList, getHistoryDetail} from "@/api/commit/generate";
import { parseTime } from '@/utils'
import {get2CommitDiff} from '@/api/github/commit'
var echarts = require('echarts');

export default {
  name: "Generate",
  data() {
    return {
      queryParams: {
        localProjectPath: null
      },
      detailParams: {
        summaryEntityJSON: null
      },
      commit_message: null,
      commit_stereotype: null,
      method_statistic: null,
      main1PieDataKey: [],
      main1PieDataValue: [],
      file_num: null,
      add_num: null,
      remove_num:null,
      changed_num:null,
      // git commit信息表格数据
      historyList: [],
      isShowLoading: false
    }
  },
  methods: {
    handleGenerate() {
      generateCommit(this.queryParams).then(response=>{
        this.commit_message = response.describe;
        this.method_statistic = response.methodStatistics;
        this.commit_stereotype = response.commitStereotype;
        this.file_num = response.fileNum;
        this.add_num = response.addNum;
        this.remove_num = response.removeNum;
        this.changed_num = response.changedNum;
        this.initMethodPie();
        // this.handleStatistics();
        this.initHistoryList();
      });
    },
    handleStatistics() {
      getStatistics(this.queryParams).then(response=>{
        this.file_num = response.fileNum;
        this.add_num = response.addNum;
        this.remove_num = response.removeNum;
        this.changed_num = response.changedNum;
      });
    },
    initHistoryList() {
      this.historyLoading = true;
      getHistoryList().then(response=>{
        this.historyList = response.rows;
      });
      this.historyLoading = false;
    },
    historyDetail(row) {
      console.log(row);
      console.log(row.summaryEntityJSON);
      this.detailParams.summaryEntityJSON = row.summaryEntityJSON;
      getHistoryDetail(this.detailParams).then(response=>{
        this.commit_message = response.describe;
        this.method_statistic = response.methodStatistics;
        console.log("this.method_statistic" + this.method_statistic);
        this.commit_stereotype = response.commitStereotype;
        this.file_num = response.fileNum;
        this.add_num = response.addNum;
        this.remove_num = response.removeNum;
        this.changed_num = response.changedNum;
        this.initMethodPie();
        this.initHistoryList();
      });
    },
    initMethodPie() {
      var jsonarr=JSON.parse( this.method_statistic );

      this.main1PieDataKey = [];
      this.main1PieDataValue = [];
      for (var i in jsonarr) {
        const obj = {
          name: i,
          value: jsonarr[i]
        }
        this.main1PieDataKey.push(obj);
        this.main1PieDataValue.push(i);
      }


      var myChart = echarts.init(document.getElementById('main1'));
      console.log(this.main1PieDataValue);
      // 绘制图表
      myChart.setOption({
        //提示框组件,鼠标移动上去显示的提示内容
        tooltip: {
          trigger: 'item',
          formatter: "{a} <br/>{b}: {c} ({d}%)"//模板变量有 {a}、{b}、{c}、{d}，分别表示系列名，数据名，数据值，百分比。
        },
        //图例
        legend: {
          orient: 'vertical',
          //data中的名字要与series-data中的列名对应，方可点击操控
          data: this.main1PieDataKey,
          textStyle: {
            //图例字体大小
            fontSize: 8,
          },
          //图例大小
          itemHeight: 8,
          //图例滚动显示
          type: 'scroll',
          right: 0,
          top: 20,
          bottom: 20,
        },
        series: [
          {
            name: 'method stereotype 统计',
            type: 'pie',
            radius: '100%',
            avoidLabelOverlap: false,
            data: this.main1PieDataKey,
            center: ['30%', '50%'],
            label: {
              normal: {
                show: true,
                position: 'inside',
                formatter: '{d}%',//模板变量有 {a}、{b}、{c}、{d}，分别表示系列名，数据名，数据值，百分比。{d}数据会根据value值计算百分比

                textStyle : {
                  align : 'center',
                  baseline : 'middle',
                  fontFamily : '微软雅黑',
                  fontSize : 10,
                  fontWeight : 'bolder'
                }
              },
            },
            itemStyle: {
              normal: {
                label: {
                  show: true,
                  position: 'outer',
                  formatter: function (p) {
                    return p.data.name;
                  }
                }
              },
              labelLine: {
                show: true
              }
            }
          }
        ]
      })
    }
  },
  mounted() {
    this.isShowLoading = true;
    // console.log(this.$route.params.jump_row )
    if (this.$route.params.jump_row != null) {
      console.log(this.$route.params.jump_row);
      this.historyDetail(this.$route.params.jump_row);
    }

    if (this.$route.query.isCommitAnalyze != null) {
      // console.log(this.$route.params.commitData);
      //
      console.log("===============")


      var commitData = new Object();
      commitData.preCommit = this.$route.query.preCommit;
      commitData.nextCommit = this.$route.query.nextCommit;
      commitData.username = this.$route.query.username;
      commitData.repoName = this.$route.query.repoName;
      console.log(commitData);
      get2CommitDiff(commitData).then(response => {
        console.log(response);
        this.commit_message = response.describe;
        this.method_statistic = response.methodStatistics;
        this.commit_stereotype = response.commitStereotype;
        this.file_num = response.fileNum;
        this.add_num = response.addNum;
        this.remove_num = response.removeNum;
        this.changed_num = response.changedNum;
        this.initMethodPie();
        this.initHistoryList();
        this.isShowLoading = false;
        // this.historyDetail(response)
      })
    }

  }
}
</script>
