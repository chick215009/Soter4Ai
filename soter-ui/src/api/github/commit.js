import request from '@/utils/request'

// 查询git commit信息列表
export function listCommit(query) {
  return request({
    url: '/github/commit/list',
    method: 'get',
    params: query
  })
}

// 查询git commit信息详细
export function getCommit(id) {
  return request({
    url: '/github/commit/' + id,
    method: 'get'
  })
}

// 新增git commit信息
export function addCommit(data) {
  return request({
    url: '/github/commit',
    method: 'post',
    data: data
  })
}

// 修改git commit信息
export function updateCommit(data) {
  return request({
    url: '/github/commit',
    method: 'put',
    data: data
  })
}

// 删除git commit信息
export function delCommit(id) {
  return request({
    url: '/github/commit/' + id,
    method: 'delete'
  })
}
