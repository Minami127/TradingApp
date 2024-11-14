from flask import request
from flask_jwt_extended import get_jwt_identity, jwt_required
from flask_restful import Resource
from config import Config
from mysql_connection import get_connection
from mysql.connector import Error
import boto3
from datetime import datetime


# 찜 상태
class IsLikeStatusResource(Resource):

    @jwt_required()
    def post(self) :

        user_id = get_jwt_identity()
        product_id = request.json.get('product_id')

        try :

            connection = get_connection()

            query = '''select (Is_valid) from favorite_product where user_id = %s and product_id = %s;'''

            record = (user_id,product_id, )
            cursor = connection.cursor()
            cursor.execute(query,record)

            result = cursor.fetchone()

            if result:
                Is_Valid = result[0]  # 결과에서 첫 번째 값만 가져옴
            else:
                Is_Valid = 0 


            cursor.close()
            connection.close()


        except Error as e:
            print(e)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500
        
        return {'result' : 'success','Is_Valid' : Is_Valid 
                },200





# 찜 추가
class IsLikeResource(Resource):

    @jwt_required()
    def post(self):

        user_id = get_jwt_identity()
        product_id = request.json.get('product_id')
        Is_valid = 1

        try: 
            connection = get_connection()

            query = '''insert into favorite_product (user_id,product_id,Is_valid) values (%s,%s,%s);'''

            record = (user_id, product_id,Is_valid)
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)
            connection.commit()

            cursor.close()
            connection.close()


        except Error as e:
            print(e)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500
        
        return {'result' : 'success',
                'user_id' : user_id,
                'product_id' : product_id,
                'Is_valid' : Is_valid
                },200
    
# 찜 제거
class DisLikeResource(Resource):

    @jwt_required()

    def delete(self,product_id) : 

        user_id = get_jwt_identity()
   

        try: 
            connection = get_connection()

            query = '''delete from favorite_product where user_id = %s and product_id = %s;'''

            record = (user_id, product_id)
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)
            connection.commit()

            cursor.close()
            connection.close()

        except Error as e:
            print(e)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500
        
        return {'result' : 'success'},200
    


# 찜 갯수
class IsLikeCountResource(Resource):

    @jwt_required()
    def get(self,product_id) :

        
        try :
            connection = get_connection()

            query = '''select id from favorite_product where product_id = %s;'''

            record = (product_id, )
            cursor = connection.cursor()
            cursor.execute(query,record)

            result_list = cursor.fetchall()
            result_list = [row[0] for row in result_list]

            cursor.close()
            connection.close

        except Error as e:
            print(e)
            cursor.close()
            connection.close
            return{'error' : str(e)},500
        
        return{'result' : 'success',
               'items' : result_list, "count" : len(result_list)},200
            


        

        





