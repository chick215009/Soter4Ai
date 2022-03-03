<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <h3>最近生成记录</h3>
        <el-table
          :data="historyList"
          @row-click="jumpHistoryDetail"
        >
          <el-table-column label="生成时间" align="center" prop="generatedDate" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.generatedDate, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="地址" align="center" prop="localProjectPath"/>
        </el-table>

      </el-col>
      <el-col :span="18">
        <div id="main1" style="width: 100%; height: 700px"></div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {getHistoryDetail, getHistoryList, getGeneratedCommitStatistics} from "@/api/commit/generate";
import * as echarts from 'echarts';


export default {
  data() {
    return {
      // git commit信息表格数据
      historyList: [],
      queryParams: {
        projectPath: null
      },
      statisticsData:[
        ['product', '2022-02-01 15:33', '2022-03-02 15:33', '2022-03-03 15:33', '2022-03-04 15:33', '2022-03-05 15:33', '2022-03-06 15:33','2022-03-07 15:33'],
        ['Milk Tea', '56', '82', '88', '70', '53', '85', '23'],
        ['Matcha Latte', '51.1', '51.4', '55.1', '53.3', '73.8', '68.7', '23']
        // ['Cheese Cocoa', 40.1, 62.2, 69.5, 36.4, 45.2, 32.5, 24],
        // ['Walnut Brownie', 25.2, 37.1, 41.2, 18, 33.9, 49.1, 26]
      ]
    }

  },
  methods: {
    jumpHistoryDetail(row) {
      this.$router.push({
        name: 'Generate',
        params: {
          jump_row: row
        },
      });
      // console.log(row);
    },
    initHistoryList() {
      getHistoryList().then(response => {
        this.historyList = response.rows;
      });
    },
    initStatisticsData() {
      getGeneratedCommitStatistics("测试地址").then(response => {
        this.statisticsData = response.data.res;
        this.initMain1(this.statisticsData);
        // console.log(response.data.res);
        // const newArr = [];
        // var obj = response.data.res;
        // for (var k in obj) {
        //   var childArr = [];
        //   for (var l in obj[k]) {
        //     childArr.push(obj[k][l]);
        //   }
        //   newArr.push(childArr);
        // }
        // console.log(newArr);
        // this.statisticsData = newArr;
        // console.log(this.statisticsData);
      });
    },
    initMain1(statisticsData) {
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
              // source: [
              //   ['product', '2022-02-01 15:33', '2022-03-02 15:33', '2022-03-03 15:33', '2022-03-04 15:33', '2022-03-05 15:33', '2022-03-06 15:33','2022-03-07 15:33'],
              //   ['Milk Tea', '56', '82', '88', '70', '53', '85', '23'],
              //   ['Matcha Latte', '51.1', '51.4', '55.1', '53.3', '73.8', '68.7', '23']
              //   // ['Cheese Cocoa', 40.1, 62.2, 69.5, 36.4, 45.2, 32.5, 24],
              //   // ['Walnut Brownie', 25.2, 37.1, 41.2, 18, 33.9, 49.1, 26]
              // ]
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
                  value: '2022-02-01 15:33',
                  tooltip: '2022-02-01 15:33'
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
                  }
                }
              });
            }
          });
          myChart.setOption(option);
        });
      })

    },
  },
  mounted() {
    this.initHistoryList();
    this.initStatisticsData();
  }
}

</script>
