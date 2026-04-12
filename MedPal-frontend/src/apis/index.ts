// 兼容层导出
// 这个文件使得旧的 `from '../apis'` 导入方式可以继续工作
// 实际 API 实现在 ../api/ 目录下

export {
  UserControllerApi,
  FileControllerApi,
  ClassesControllerApi,
  ClassChatControllerApi,
  NewsControllerApi,
  QuestionControllerApi,
  QuestionAssignmentControllerApi,
  QuestionSubmissionControllerApi,
  CommentsControllerApi,
  VideoControllerApi,
  DeleteRequest,
  UserQueryRequest,
  UserAddRequest,
  UserUpdateRequest,
  UserLoginRequest,
  UserRegisterRequest,
  NewsQueryRequest,
  NewsAddRequest,
  NewsUpdateRequest,
  QuestionQueryRequest,
  QuestionAddRequest,
  QuestionUpdateRequest,
} from '../api/compat';
