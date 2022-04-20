<template>
  <div>
<!--    <el-col :span=10>-->
<!--      <v-treeview-->
<!--        v-model="treeData"-->
<!--        :treeTypes="treeTypes"-->
<!--        @selected="selected"-->
<!--        :openAll="openAll"-->
<!--        :contextItems="contextItems"-->
<!--        @contextSelected="contextSelected"-->
<!--      >-->

<!--      </v-treeview>-->
<!--    </el-col>-->
    <el-col :span="7">
      <el-input
        placeholder="搜索文件名"
        v-model="filterText">
      </el-input>
      <el-tree
        default-expand-all
        :data="data"
        :props="defaultProps"
        @node-click="handleNodeClick"
      >

      </el-tree>
    </el-col>
    <el-col :span="17">
      <template>
        <!-- If your source-code lives in a variable called 'sourcecode' -->
        <pre
          v-highlightjs="content"
        ><code class="java"></code></pre>
      </template>
    </el-col>
<!--    <el-col :span=6>-->
<!--      <el-select-->
<!--        v-model="value1"-->
<!--        placeholder="请选择版本号"-->
<!--      >-->
<!--        <el-option-->
<!--          v-for="item in options"-->
<!--          :key="item.value"-->
<!--          :label="item.label"-->
<!--          :value="item.value">-->
<!--        </el-option>-->
<!--      </el-select>-->
<!--    </el-col>-->
<!--    <el-col :span=8>-->
<!--      <el-table-->
<!--        :data="tableData"-->
<!--        style="width: 100%"-->
<!--        @row-click="jumpFileContent"-->
<!--      >-->
<!--        <el-table-column-->
<!--          prop="path"-->
<!--          label="文件"-->
<!--        >-->
<!--        </el-table-column>-->
<!--      </el-table>-->
<!--    </el-col>-->


  </div>
</template>

<script>
import Vue from 'vue'
import VueHighlightJS from 'vue-highlightjs'
import VTreeview from "v-treeview"
import {analyzeDisplayProjectTags} from "@/api/github/commit";
Vue.use(VueHighlightJS)

import 'highlight.js/styles/atom-one-dark.css'
import {getFileContent} from '@/api/code/code'
export default {
  name: "index",
  data() {
    return {
      options: [],
      value1: '',
      tableData: [
        {
          path: "abc/wer/A.java",
          // url: "http://localhost:1024/code/change?localpath=%2FUsers%2Fchengleming%2FMasterThesis%2FSoter%2Ftmp%2FRuoYi%2Fbase%2Fruoyi-admin%2Fsrc%2Fmain%2Fjava%2Fcom%2Fruoyi%2Fweb%2Fcontroller%2Fcommon%2FCommonController.java"
        }
      ],
      openAll: true,
      filterText: null,
      content: '123',
      params: {
        localProjectPath: '/Users/chengleming/work/projectDir/ruoyi-admin/src/main/java/com/ruoyi/web/controller/common/CommonController.java'
      },
      data: [{
        label: 'com',
        children: [{
          label: 'ruoyi',
          children: [{
            label: 'web',
            children: [{
              label: 'controller',
              children: [
                {
                  label: 'common',
                  children: [{
                    label: 'CommonController.java'
                },{
                  label: 'demo',
                  children: [
                    {
                      label: 'controller',
                      children: [
                        {
                          label: 'DemoDialogController.java'
                        },
                        {
                          label: 'DemoFormController.java'
                        },
                        {
                          label: 'DemoIconController.java'
                        },
                        {
                          label: 'DemoOperateController.java'
                        },
                        {
                          label: 'DemoReportController.java'
                        },
                        {
                          label: 'DemoTableController.java'
                        }
                      ]
                    }
                  ]
                }]
              },
                {
                  label: 'monitor',
                  children: [
                    {
                      label: 'CacheController.java'
                    },
                    {
                      label: 'DruidController.java'
                    },
                    {
                      label: 'ServerController.java'
                    }
                  ]
                },
                {
                  label: 'tool',
                  children: [
                    {
                      label: 'BuildController.java'
                    }
                  ]
                }
              ]
            }]
          }]
        }]
      }],
      // , {
      //   label: '一级 2',
      //   children: [{
      //     label: '二级 2-1',
      //     children: [{
      //       label: '三级 2-1-1'
      //     }]
      //   }, {
      //     label: '二级 2-2',
      //     children: [{
      //       label: '三级 2-2-1'
      //     }]
      //   }]
      // }, {
      //   label: '一级 3',
      //   children: [{
      //     label: '二级 3-1',
      //     children: [{
      //       label: '三级 3-1-1'
      //     }]
      //   }, {
      //     label: '二级 3-2',
      //     children: [{
      //       label: '三级 3-2-1'
      //     }]
      //   }]
      // }],
      treeTypes: [
        {
          type: "#",
          max_children: 6,
          max_depth: 4,
          valid_children: [
            "FMM_EMPLOYEE",
            "FMM_SPOUSE",
            "FMM_CHILD",
            "FMM_SIBLING",
            "FMM_PARENT",
            "FMM_PARENT_IN_LAW"
          ]
        },
        {
          type: "FMM_EMPLOYEE",
          icon: "far fa-user",
          valid_children: ["Basic", "Top-up"]
        },
        {
          type: "FMM_SPOUSE",
          icon: "far fa-user",
          valid_children: ["Basic", "Top-up"]
        },
        {
          type: "FMM_CHILD",
          icon: "far fa-user",
          valid_children: ["Basic", "Top-up"]
        },
        {
          type: "FMM_SIBLING",
          icon: "far fa-user",
          valid_children: ["Basic", "Top-up"]
        },
        {
          type: "FMM_PARENT",
          icon: "far fa-user",
          valid_children: ["Basic", "Top-up"]
        },
        {
          type: "FMM_PARENT_IN_LAW",
          icon: "far fa-user",
          valid_children: ["Basic", "Top-up"]
        },
        {
          type: "Basic",
          icon: "far fa-hospital",
          valid_children: ["Top-up"]
        },
        {
          type: "Top-up",
          icon: "far fa-plus-square",
          valid_children: []
        }
      ],
      treeData: [
        {
          id: 100767.0,
          text: "Employee",
          type: "FMM_EMPLOYEE",
          count: 0,
          children: [
            {
              id: 100811.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: [
                {
                  id: 101161.0,
                  text: "Top-up",
                  type: "Top-up",
                  count: 152,
                  children: []
                }
              ]
            },
            {
              id: 100812.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: []
            },
            {
              id: 101162.0,
              text: "This Top-up can be at level 2",
              type: "Top-up",
              count: 152,
              children: []
            }
          ]
        },
        {
          id: 100768.0,
          text: "Spouse",
          type: "FMM_SPOUSE",
          count: 0,
          children: [
            {
              id: 100813.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: [
                {
                  id: 101163.0,
                  text: "Top-up",
                  type: "Top-up",
                  count: 152,
                  children: []
                }
              ]
            },
            {
              id: 100814.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: [
                {
                  id: 101164.0,
                  text: "Top-up",
                  type: "Top-up",
                  count: 152,
                  children: []
                }
              ]
            }
          ]
        },
        {
          id: 100769.0,
          text: "Child",
          type: "FMM_CHILD",
          count: 0,
          children: [
            {
              id: 100815.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: [
                {
                  id: 101165.0,
                  text: "Top-up",
                  type: "Top-up",
                  count: 152,
                  children: []
                }
              ]
            },
            {
              id: 100816.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: [
                {
                  id: 101166.0,
                  text: "Top-up",
                  type: "Top-up",
                  count: 0,
                  children: []
                }
              ]
            }
          ]
        },
        {
          id: 100770.0,
          text: "Parents",
          type: "FMM_PARENT",
          count: 0,
          children: [
            {
              id: 100817.0,
              text: "Basic plan",
              type: "Basic",
              count: 0,
              children: [
                {
                  id: 101167.0,
                  text: "Top-up",
                  type: "Top-up",
                  count: 124,
                  children: []
                }
              ]
            }
          ]
        }
      ],
      contextItems: [],
      selectedNode: null
    }
  },
  methods: {
    initFileList(params) {
      getFileList(params).then(response => {
         let paths = response.data;
         console.log(paths);
         this.tableData = [];
         for (let i in paths) {
           let obj = {
             path: paths[i]
           }
           console.log(obj);
           this.tableData.push(obj);
         }
      });
    },
    getTypeRule(type) {
      var typeRule = this.treeTypes.filter(t => t.type == type)[0];
      return typeRule;
    },
    contextSelected(command) {
      switch (command) {
        case "Create Basic":
          var node = {
            text: "New Basic Plan",
            type: "Basic",
            children: []
          };
          this.selectedNode.addNode(node);
          break;
        case "Create Top-up":
          var node = {
            text: "New Top-up",
            type: "Top-up",
            children: []
          };
          this.selectedNode.addNode(node);
          break;
        case "Rename":
          this.selectedNode.editName();
          break;
        case "Remove":
          break;
      }
    },
    selected(node) {
      this.selectedNode = node;
      this.contextItems = [];
      var typeRule = this.getTypeRule(this.selectedNode.model.type);
      typeRule.valid_children.map(function(type, key) {
        var childType = this.getTypeRule(type);
        var item = {
          title: "Create " + type,
          icon: childType.icon,
          type: childType
        };
        this.contextItems.push(item);
      }, this);

      this.contextItems.push({ title: "Rename", icon: "far fa-edit" });
      this.contextItems.push({ title: "Remove", icon: "far fa-trash-alt" });
    },
    jumpFileContent(row) {
      window.open("http://localhost:1024/code/change?localpath=" + row.path, "_blank")
    },
    initTags(params) {
      analyzeDisplayProjectTags(params).then(response => {
        this.options = response.data;
      })
    },
    handleNodeClick(data) {
      console.log(data);
    },
    readFileContent(param) {
      getFileContent(param).then(response => {
        console.log(response.msg);
        this.content = response.msg;
      })
    }
  },
  mounted() {
    // let params = this.$route.query.params;
    // let fileParams = JSON.parse(params);
    // this.initTags(fileParams);
    // this.initFileList(fileParams);
    this.readFileContent(this.params);
  },
  components: {
    VTreeview
  }
}
</script>

<style scoped>

</style>
