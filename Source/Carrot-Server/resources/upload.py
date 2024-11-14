from flask import request
from flask_jwt_extended import get_jwt_identity, jwt_required
from flask_restful import Resource
from config import Config
from mysql_connection import get_connection
from mysql.connector import Error
import boto3
from datetime import datetime
import boto3


class FileUploadResource(Resource) :

    @jwt_required()
    def post(self) :

        file = request.files.get('photo')
        product_id = request.form.get('product_id')

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
        

        imageUrl = Config.S3_LOCATION + file.filename

        try :
            connection =  get_connection()

            query = '''insert into product_image (product_id,product_image_url) values (%s, %s);'''

            record = (product_id, imageUrl)

            print(record)

            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)
            connection.commit()

            cursor.close()
            connection.close()



        except Exception as e :
            print(Error)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500 

        
        return {'result' : 'success' , 
                'imgUrl' : Config.S3_LOCATION + file.filename}  
    
    



