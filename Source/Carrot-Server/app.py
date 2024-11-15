import serverless_wsgi

import os
os.environ["JOBLIB_MULTIPROCESSING"] = "0"
os.environ["HOME"] = "/tmp"

from flask import Flask
from flask_jwt_extended import JWTManager
from flask_restful import Api
from config import Config

from resources.user import CheckEmailResource, UserLoginResource, UserLogoutResource, UserNameResource, UserProfileImageResource, UserRegisterResource, UserinformationResource
from resources.upload import FileUploadResource
from resources.post import DelectPostResource, MyPostListResource, PostAddResource, PostDetailResource, PostFavoriteListResource, PostImageViewResource, PostListResource, PostViewCountResource, SearchTitleResource, StatusResource
from resources.islike import DisLikeResource, IsLikeCountResource, IsLikeResource, IsLikeStatusResource
from resources.category import CategoryListResource, CategoryResource

from flask import Flask, jsonify, make_response

app = Flask(__name__)

api = Api(app)


# 환경변수 셋팅

app.config.from_object(Config)

#jwt 매니저 초기화
jwt = JWTManager(app)


api.add_resource(UserRegisterResource, '/user/register')
api.add_resource(UserLoginResource,'/user/login')
api.add_resource(FileUploadResource,'/uploadimg')
api.add_resource(UserLogoutResource,'/user/logout')
api.add_resource(PostListResource,'/post/list')
api.add_resource(PostDetailResource,'/post/detail/<int:id>') # 상세보기
api.add_resource(PostAddResource,'/post/add')
api.add_resource(PostImageViewResource, '/post/image/<int:product_id>')
api.add_resource(IsLikeResource,'/IsLike')
api.add_resource(DisLikeResource,'/DisLike/<int:product_id>')
api.add_resource(IsLikeCountResource,'/likeCnt/<int:product_id>')
api.add_resource(CheckEmailResource,'/check')
api.add_resource(IsLikeStatusResource,'/like/status')
api.add_resource(PostFavoriteListResource,'/favorite/list')
api.add_resource(CategoryResource, '/category/<int:id>')
api.add_resource(CategoryListResource, '/categorylist')
api.add_resource(PostViewCountResource,'/viewCnt/<int:id>')
api.add_resource(MyPostListResource,'/mylist')
api.add_resource(DelectPostResource,"/delete/<int:product_id>")
api.add_resource(UserNameResource, "/user/nickname/<int:id>")
api.add_resource(UserProfileImageResource, "/user/profileimg/<int:id>")
api.add_resource(SearchTitleResource,'/post/search')
api.add_resource(UserinformationResource,'/user/info/<int:id>')
api.add_resource(StatusResource,"/post/state/<int:product_id>")



@app.route("/")
def hello_from_root():
    return jsonify(message='Hello from root!')


@app.route("/hello")
def hello():
    return jsonify(message='Hello from path!')


@app.errorhandler(404)
def resource_not_found(e):
    return make_response(jsonify(error='Not found!'), 404)

def handler(event, context):
    return serverless_wsgi.handle_request(app,event,context)



if __name__ == '__main__' :
    app.run()

