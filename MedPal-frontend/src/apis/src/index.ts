// 兼容层导出 (apis/src/index.ts)
// 这个文件使得 `from '../../apis/src'` 导入方式可以继续工作

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
} from '../../api/compat';
