import request from '@/utils/request'

// 查询git commit信息列表
export function generateCommit(queryParams) {
  return request({
    url: '/commit/generate/analyze',
    method: 'get',
    params: queryParams

  })
}
