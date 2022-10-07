<template>
  <div>
    <div
      style="margin-left: 40%"
    >
      <el-switch
        v-model="value1"
        active-text="side-by-side"
        inactive-text="line-by-line"
        on-value="1"
        off-value="0"
        @change="switchChange()"
      />
    </div>


    <div>
      <code-diff
        :old-string="oldStr"
        :new-string="newStr"
        :context="10"
        :outputFormat="output_format"
      />
    </div>
  </div>
</template>

<script>
import CodeDiff from "vue-code-diff";
import {getFileContent} from "@/api/code/code";
export default {
  components: { CodeDiff },
  data() {
    return {
      projectPath: "/Users/chengleming/work/projectDir",
      output_format: "side-by-side",
      value1: true,
      oldStr: "",
      newStr: "",
      content:'',
      tableData: [],
      params: {
        localProjectPath: ''
      }
    };
  },
  methods: {

    // readFile1(file) {
    //   // TODO: file.type === 'XXX' 校验是否是指定的文本文件
    //   let reader = new FileReader();
    //   reader.readAsText(file.raw);
    //   reader.onload = (e) => {
    //     // 读取文件内容
    //     this.oldStr = e.target.result;
    //   };
    // },

    // readFile2(file) {
    //   // TODO: file.type === 'XXX' 校验是否是指定的文本文件
    //   let reader = new FileReader();
    //   reader.readAsText(file.raw);
    //   reader.onload = (e) => {
    //     // 读取文件内容
    //     this.newStr = e.target.result;
    //   };
    // },

    switchChange() {
      if (this.output_format === "side-by-side") {
        this.value1 = false;
        this.output_format = "line-by-line";
      } else {
        this.value1 = true;
        this.output_format = "side-by-side";
      }
    },
    // handleChangedClassList() {
    //   this.$axios.get('/dev/get-changed-classes', {
    //     params: {
    //       projectPath: this.projectPath
    //     }
    //   }).then(response => {
    //     this.tableData = response.data;
    //
    //   }).catch(function(response){
    //     console.log(response);
    //   });
    // },
    // compare(row, event, column) {
    //   let rowClassPath = row.classPath;
    //   this.$axios.get('/dev/get-adjacent-projects', {
    //     params: {
    //       classPath: row.classPath,
    //       projectPath: this.projectPath
    //     }
    //   }).then(response => {
    //     this.oldStr = response.data[0];
    //     this.newStr = response.data[1];
    //
    //   }).catch(function(response){
    //     console.log(response);
    //   });
    // },

    readFileContent(param) {
      getFileContent(param).then(response => {
        // console.log(response.msg);
        this.content = response.msg;
      })
    }
  },

  mounted() {
    // this.handleChangedClassList();
    // console.log("1111" + this.tableData);
    setTimeout(async () => {
      this.params.localProjectPath='/Users/chengleming/MasterThesis/code-diff/a/AddressUtils.java';
      getFileContent(this.params).then(response => {
        // console.log(response.msg);
        this.oldStr = response.msg;
      })
      this.params.localProjectPath='/Users/chengleming/MasterThesis/code-diff/b/AddressUtils.java';
      getFileContent(this.params).then(response => {
        // console.log(response.msg);
        this.newStr = response.msg;
      })
    }, 1000);


    // this.readFileContent(this.params);
    // console.log(this.content);
    // this.oldStr = this.content;
    // console.log(this.oldStr);
    // this.params.localProjectPath='/Users/chengleming/MasterThesis/code-diff/b/AddressUtils.java';
    // this.readFileContent(this.params);
    // console.log(this.content);
    // // this.newStr = '456'
    // this.newStr = this.content;
    // console.log(this.newStr);
  }
};
</script>
