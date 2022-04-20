import request from '@/utils/request'

export function getFileContent(params) {
  return request( {
    url: '/commit/generate/content',
    method: 'get',
    params: params
  })
}

export function getFileList(params) {
  return request( {
    url: '/commit/generate/files',
    method: 'get',
    params: params
  })
}
