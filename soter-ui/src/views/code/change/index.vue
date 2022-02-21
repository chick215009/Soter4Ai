<template>
  <el-form model="queryParams" ref="queryFrom" label-width="68px">
    <el-form-item>
      <el-input
        v-model="queryParams.localProjectPath"
        placeholder="请输入本地项目地址"
        clearable
        size="small"
        v-on:input="upgradeLocalProjectPath"
      />
    </el-form-item>
  </el-form>

</template>

<script>
export default {
  data() {
    return {
      queryParams:{
        localProjectPath:""
      }
    }
  },
  methods: {
    upgradeLocalProjectPath() {
      this.$store.state.localProjectPath = this.queryParams.localProjectPath;
    },
    paramsInit() {
      this.queryParams.localProjectPath = this.$store.state.localProjectPath;
    }
  },
  created() {
    paramsInit()
  }
}

</script>
