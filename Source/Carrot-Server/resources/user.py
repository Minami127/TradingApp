from datetime import datetime
import boto3
from flask import request
from config import Config
from flask_jwt_extended import create_access_token, get_jwt, get_jwt_identity, jwt_required
from flask_restful import Resource
from mysql_connection import get_connection
from mysql.connector import Error

from email_validator import EmailNotValidError, validate_email

from utils import check_password, hash_password

class UserRegisterResource(Resource) :

    def post(self) :

        data = request.get_json()

        try : 
            validate_email(data['email'])
        except EmailNotValidError as e :
            print(e)
            return{'error' : str(e)}, 400
        
        if len(data['password']) < 4 or len(data['password']) > 12 :
            return {'error' : '비밀번호 길이를 확인하세요'}, 400
        
        password = (data['password'])

        print(password)


        try :
            connection = get_connection()
            
            # 이메일 중복 검사 쿼리
            check_query = '''SELECT * FROM users WHERE email = %s'''
            cursor = connection.cursor(dictionary=True)
            cursor.execute(check_query, (data['email'],))
            result = cursor.fetchone()

            if result:
                cursor.close()
                connection.close()
                return {'error': '이미 존재하는 이메일입니다.'}, 400
            
            # 이메일 중복 검사 쿼리
            check_query = '''SELECT * FROM users WHERE nickname = %s'''
            cursor = connection.cursor(dictionary=True)
            cursor.execute(check_query, (data['nickname'],))
            result = cursor.fetchone()

            if result:
                cursor.close()
                connection.close()
                return {'error': '이미 존재하는 닉네임입니다.'}, 400

            query = '''insert into users
                        (email,password,nickname)
                        values
                        (%s,%s,%s);'''
            
            record = (data['email'],
                      password,
                      data['nickname'])
            
            cursor = connection.cursor()
            cursor.execute(query,record)
            connection.commit()

            user_id = cursor.lastrowid

            cursor.close()
            connection.close()

        except Error as e :
            print(e)
            cursor.close()
            connection.close()
            return{"error" : str(e)}, 500
        
        access_token = create_access_token(user_id)

        return{'result' : 'success',
               'accessToken' : access_token},200
    

class UserLoginResource(Resource) :
     
    def post(self) :
         
        data = request.get_json()

        try :
             
            connection = get_connection()

            query = '''select * from users
                        where email = %s;'''
             
            record = (data['email'] , )

            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)

            result_list = cursor.fetchall()
            print(result_list)

            cursor.close()
            connection.close()

        except Error as e :
            print(e)
            cursor.close()
            connection.close()
            return{"error" : str(e)}, 500
        
        # 회원 가입을 안한경우, 리스트에 데이터가 없다.
        
        if len(result_list) == 0 :
            return {"error" : "없는 회원 입니다."}, 400
        
        # 회원 ID 정보가 일치하였으니, 비밀번호를 체크한다.
        # 로그인한 사람이 마지막에 입력한 비밀번호 data['password']
        # 회원가입할때 입력했던, 암호화된 비밀번호 DB에있음
        # result_list에 들어있고
        # 리스트의 첫번째 데이터에 들어있다

        check = data['password'], result_list[0]['password']

        if check == False :
            return {"error" : "비밀번호가 틀립니다."}, 400
        
        access_token = create_access_token(result_list[0]['id'])

        return {"result" : "success"
                ,"userId" : result_list[0]['id']
                ,"profileImg" : result_list[0]['profile_img']
                ,"nickname" : result_list[0]['nickname']
                ,"accessToken" : access_token}
    
jwt_blocklist = set()
class UserLogoutResource(Resource):
    #jwt 필수
    @jwt_required()
    def delete(self):
        jti = get_jwt()['jti']
        print(jti)
        
        jwt_blocklist.add(jti)


        return {"result" : "success"}, 200
    

class CheckEmailResource(Resource):

    def get(self):
      
        email = request.args.get('email')

        if not email:
            return {"error": "이메일을 제공해야 합니다."}, 400

        try:
            connection = get_connection()

            query = '''SELECT * FROM users WHERE email = %s;'''

            record = (email,)
            print(email)
            print(f"Received email: {email}")
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)

            result_list = cursor.fetchall()
            cursor.close()
            connection.close()

            if result_list:
                return {"result": "success", "exists": True, "email": result_list[0]['email']}, 200
            else:
                return {"result": "success", "exists": False}, 404  # 사용자 존재하지 않음

        except Error as e:
            print(e)
            return {"error": str(e)}, 500

        finally:
            if cursor:
                cursor.close()
            if connection:
                connection.close()

class UserNameResource(Resource):

    @jwt_required()
    def put(self, id):
        try:
            connection = get_connection()

            # 업데이트할 viewCnt 값을 request로부터 가져옴
            nickname = request.json.get('nickname')  

            if nickname is None:
                return {"error": "viewCnt value is required"}, 400

            query = '''update users
                        set nickname = %s
                        where id = %s'''

            record = (nickname, id)
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query, record)
            connection.commit()

            cursor.close()
            connection.close()

        except Error as e:
            return {"error": str(e)}, 500
        

        return {"result": "success"}, 200
    


class UserProfileImageResource(Resource):

    @jwt_required()
    def put(self, id):

        file = request.files.get('photo')

        if file is None :
            return {'error' : '파일을 업로드 하세요'}, 400
        
        current_time = datetime.now()

        new_file_name = current_time.isoformat().replace(':', '_') + '.jpg'  

        file.filename = new_file_name

        s3 = boto3.client('s3',
                    aws_access_key_id = Config.AWS_ACCESS_KEY_ID,
                    aws_secret_access_key = Config.AWS_SECRET_ACCESS_KEY )

        try :
            s3.upload_fileobj(file, 
                              Config.S3_BUCKET,
                              file.filename,
                              ExtraArgs = {'ContentType' : 'image/jpeg'} )  
            
        except Exception as e :
            print(e)
            return {'error' : str(e)}, 500
        

        profile_img = Config.S3_LOCATION + file.filename

        try:

            connection = get_connection()


            if profile_img is None:
                return {"error": "viewCnt value is required"}, 400

            query = '''update users
                        set  profile_img = %s
                        where id = %s'''

            record = (profile_img, id)
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query, record)
            connection.commit()

            cursor.close()
            connection.close()

        except Error as e:
            return {"error": str(e)}, 500
        

        return {"result": "success"}, 200
    

class UserinformationResource(Resource) :


    def get(self,id) :

        try :
            connection = get_connection()

            query = '''select * from users where id = %s;'''

            record = (id, )
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)

            result_list = cursor.fetchall()
            cursor.close()
            connection.close()

            access_token = create_access_token(result_list[0]['id'])

        except Error as e:
            return {"error": str(e)}, 500


        return {"result" : "success"
                ,"userId" : result_list[0]['id']
                ,"profileImg" : result_list[0]['profile_img']
                ,"nickname" : result_list[0]['nickname']
                ,"accessToken" : access_token}








    
        



             

         


            




