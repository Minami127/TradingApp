from flask import request
from flask_jwt_extended import get_jwt_identity, jwt_required
from flask_restful import Resource
from config import Config
from mysql_connection import get_connection
from mysql.connector import Error
import boto3
from datetime import datetime


class CategoryResource(Resource) :

    @jwt_required()
    def get(self, id):

        try :
            connection = get_connection()

            query = '''select id,name from category where id = %s;'''
            
            record = (id, )
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)

            result_list = cursor.fetchall()

            cursor.close()
            connection.close()

        except Error as e:
            print(Error)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500 
        
        return{'result' : 'success',
                'items' : result_list},200
    
class CategoryListResource(Resource) :

    @jwt_required()
    def get(self):

        try :
            connection = get_connection()

            query = '''select id,name from category;'''
            
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query)

            result_list = cursor.fetchall()

            cursor.close()
            connection.close()
            

        except Error as e:
            print(Error)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500 
        
        
        return{'result' : 'success',
                'items' : result_list},200