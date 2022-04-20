
<template xmlns="http://www.w3.org/1999/html">
  <div
    class="app-container"
    v-loading="isShowLoading"
    element-loading-text="分析项目中"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.2)"
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
                <el-form :model="queryParams">
                  <el-row  >
                    <span> https://github.com/ </span>
                    <el-input placeholder="请输入GitHub项目作者" v-model="queryParams.username" style="width: 175px">
                    </el-input>
                    <span> / </span>
                    <el-input placeholder="请输入GitHub项目仓库名" v-model="queryParams.repoName" style="width: 175px">
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
<!--              <el-descriptions-item label="项目名">RuoYi</el-descriptions-item>-->
<!--              <el-descriptions-item label="GitHub URL地址">https://github.com/yangzongzhuan/RuoYi</el-descriptions-item>-->
              <el-descriptions-item label="最新版本">v4.7.3</el-descriptions-item>
              <el-descriptions-item label="最后一次提交">2022-03-01</el-descriptions-item>
            </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <div style="margin-left: 20px">
          <h3>1.请选择GitHub项目需要分析的版本（多选）</h3>
          <div>
            <el-row>
              <el-select
                v-model="value1"
                multiple placeholder="请选择需要分析的版本号"
                @visible-change="displayTags($event)"
                style="width: 200px"
              >
                <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-row>
          </div>
          <h3>2.分析GitHub项目版本之间的元素模式变化</h3>
          <div>
            <el-row>
              <el-button
                @click="handleAnalyze"
                type="primary"
                size="small"
                icon="el-icon-search"
              >
                分析指定版本
              </el-button>
            </el-row>
          </div>
        </div>

      </el-col>
    </el-row>



<!--    <h3>1. 请输入GitHub项目-->
<!--      <b style="color: #1a7bb9">仓库名</b>-->
<!--        <el-switch-->
<!--          v-model="isURL"-->
<!--          active-color="#13ce66"-->
<!--          inactive-color="#33ccff"-->
<!--        >-->
<!--        </el-switch>-->
<!--      <b style="color: #1a7bb9">URL</b>-->
<!--    </h3>-->




<!--    <el-row>-->
<!--      <el-form-->
<!--        :model="queryParams"-->
<!--        label-width="68px"-->
<!--      >-->
<!--        <el-form-item label="用户名" prop="username">-->
<!--          <el-input-->
<!--            v-model="queryParams.username"-->
<!--            placeholder="请输入用户名"-->
<!--            clearable-->
<!--            size="small"-->
<!--          />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="仓库名" prop="repoName">-->
<!--          <el-input-->
<!--            v-model="queryParams.repoName"-->
<!--            placeholder="请输入仓库名"-->
<!--            clearable-->
<!--            size="small"-->
<!--          />-->
<!--        </el-form-item>-->
<!--      </el-form>-->
<!--    </el-row>-->
<!--    <h3>2.请选择GitHub项目需要分析的版本（多选）</h3>-->
<!--    <div>-->
<!--    <el-row>-->
<!--      <el-select-->
<!--        v-model="value1"-->
<!--        multiple placeholder="请选择需要分析的版本号"-->
<!--        @visible-change="displayTags($event)"-->
<!--      >-->
<!--        <el-option-->
<!--          v-for="item in options"-->
<!--          :key="item.value"-->
<!--          :label="item.label"-->
<!--          :value="item.value">-->
<!--        </el-option>-->
<!--      </el-select>-->
<!--    </el-row>-->
<!--    </div>-->

<!--    <h3>3.分析GitHub项目版本之间的元素模式变化</h3>-->
<!--    <div>-->
<!--      <el-row>-->
<!--        <el-button-->
<!--          @click="handleAnalyze"-->
<!--        >-->
<!--          分析GitHub中的项目-->
<!--        </el-button>-->
<!--      </el-row>-->
<!--    </div>-->

    <el-row>
      <div id="main1" style="width: 100%; height: 700px"></div>
    </el-row>
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
import {analyzeGithubProject, analyzeDisplayProjectTags} from "@/api/github/commit";
import * as echarts from "echarts";
export default {
  name: "File",
  data() {
    return {
      queryParams: {
        username: null,
        repoName: null,
        tags:[]
      },
      repoURL: null,
      isURL: true,
      isShowLoading: false,
      options: [],
      value1:[],
      statisticsData:[]
    }

  },
  methods: {
    handleAnalyze() {
      this.isShowLoading = true;
      this.queryParams.tags = this.value1;
      console.log(this.queryParams);
      analyzeGithubProject(this.queryParams).then(response => {
        this.isShowLoading = false;
        this.statisticsData = response.data.res;
        this.initMain1(this.statisticsData, this.queryParams);
      });

    },
    displayTags(visible) {
      if (visible) {
        this.isShowLoading = true;
        analyzeDisplayProjectTags(this.queryParams).then(response => {
          console.log(response);
          this.options = response.data;
          this.isShowLoading = false;
        })
      }
    },
    initMain1(statisticsData, qparams) {
      // 新建一个promise对象
      let newPromise = new Promise((resolve) => {
        resolve()
      })
      //然后异步执行echarts的初始化函数
      newPromise.then(() => {
        //	此dom为echarts图标展示dom
        var chartDom = document.getElementById('main1');
        var myChart = echarts.init(chartDom);
        var option;
        setTimeout(function () {
          option = {
            legend: {},
            tooltip: {
              trigger: 'axis',
              showContent: false
            },
            dataset: {
              source: statisticsData
            },
            xAxis: {type: 'category'},
            yAxis: {gridIndex: 0},
            grid: {top: '55%'},
            series: [
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'line',
                smooth: true,
                seriesLayoutBy: 'row',
                emphasis: {focus: 'series'}
              },
              {
                type: 'pie',
                id: 'pie',
                radius: '30%',
                center: ['50%', '25%'],
                emphasis: {
                  focus: 'self'
                },
                label: {
                  formatter: '{b}: {@2012} ({d}%)'
                },
                encode: {
                  itemName: 'product',
                  value: '2022-02-01 15:33:11',
                  tooltip: '2022-02-01 15:33:11'
                }
              }
            ]
          };
          myChart.on('updateAxisPointer', function (event) {
            const xAxisInfo = event.axesInfo[0];
            if (xAxisInfo) {
              const dimension = xAxisInfo.value + 1;
              myChart.setOption({
                series: {
                  id: 'pie',
                  label: {
                    formatter: '{b}: {@[' + dimension + ']} ({d}%)'
                  },
                  encode: {
                    value: dimension,
                    tooltip: dimension
                  },
                }
              });
            }
          });
          myChart.setOption(option);
          myChart.on('click', function (params) {
            console.log(params);
            let jumpParams = {
              username: null,
              repoName: null,
              tags:[],
              stereoType: null
            }
            jumpParams.username = qparams.username;
            jumpParams.repoName = qparams.repoName;
            jumpParams.tags = qparams.tags;
            jumpParams.stereoType = params.data[0];
            let astr = JSON.stringify(jumpParams)
            window.open('http://localhost:1024/code/code/github/files?params=' + astr,'_blank');
          })
        });
      })

    },
  },
}
</script>
