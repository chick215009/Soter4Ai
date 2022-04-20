<template>
  <el-col :span="18">
    <div id="main1" style="width: 100%; height: 700px"></div>
  </el-col>

</template>

<script>
import {getGeneratedCommitStatistics} from "@/api/commit/generate";
import * as echarts from "echarts";

export default {
  name: "Current",
  data() {
    return {
      queryParams:{
        localProjectPath:null,
      },
      statisticsData:[
        // ['product', '2022-02-01 15:33', '2022-03-02 15:33', '2022-03-03 15:33', '2022-03-04 15:33', '2022-03-05 15:33', '2022-03-06 15:33','2022-03-07 15:33'],
        // ['Milk Tea', '56', '82', '88', '70', '53', '85', '23'],
        // ['Matcha Latte', '51.1', '51.4', '55.1', '53.3', '73.8', '68.7', '23']
        // ['Cheese Cocoa', 40.1, 62.2, 69.5, 36.4, 45.2, 32.5, 24],
        // ['Walnut Brownie', 25.2, 37.1, 41.2, 18, 33.9, 49.1, 26]
      ]
    }
  },
  methods: {
    initStatisticsData() {
      console.log(this.$route.params);
      this.queryParams.localProjectPath = this.$route.params.project_path;
      getGeneratedCommitStatistics(this.queryParams).then(response => {
        this.statisticsData = response.data.res;
        console.log(this.statisticsData);
        this.initMain1(this.statisticsData);
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
    this.initStatisticsData();
  }
}

</script>
