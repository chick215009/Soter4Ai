import request from '@/utils/request'
import * as url from "url";

// 查询git commit信息列表
export function generateCommit(queryParams) {
  return request({
    url: '/commit/generate/analyze',
    method: 'get',
    params: queryParams

  })
}

// 查询变化的统计情况
export function getStatistics(queryParams) {
  return request({
    url: '/commit/generate/statistics',
    method: 'get',
    params: queryParams
  })
}

// 查询变化的统计情况
export function getHistoryList() {
  return request({
    url: '/commit/generate/history',
    method: 'get'
  })
}

// 根据项目查询历史
export function getHistoryListByProject(queryParams) {
  return request({
    url: 'commit/generate/project_history',
    method: 'get',
    params: queryParams
  })
}

//获取所有项目
export function getProjectList() {
  return request({
    url: 'commit/generate/project_list',
    method: 'get'
  })
}

//查询method统计记录
export function getGeneratedCommitStatistics(queryParams) {
  return request( {
    url: '/commit/host',
    method: 'get',
    params: queryParams
  })
}

//查询历史详情
export function getHistoryDetail(detailParams) {
  return request( {
    url: '/commit/generate/detail',
    method: 'post',
    data: detailParams
  })
}


