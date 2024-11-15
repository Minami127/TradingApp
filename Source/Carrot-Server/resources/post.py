from flask import request
from flask_jwt_extended import get_jwt_identity, jwt_required
from flask_restful import Resource
from config import Config
from mysql_connection import get_connection
from mysql.connector import Error
import boto3
from datetime import datetime


class PostListResource(Resource):
    
    @jwt_required()
    def get(self) :
        id = get_jwt_identity()
        offset = request.args.get('offset')
        limit = request.args.get('limit')
        product_status = 1

        try :
            connection = get_connection()

            query = '''select p.id,seller_id,category_id,title,price,description,product_state,c.name,i.product_image_url,p.created_at,p.updated_at,min(i.id) as img_id
                        from products p
                        LEFT JOIN (SELECT product_id, MIN(id) as min_img_id
                            FROM product_image
                            GROUP BY product_id) img_min ON p.id = img_min.product_id
                        LEFT JOIN product_image i ON i.id = img_min.min_img_id
                        LEFT JOIN users u ON p.seller_id = u.id
                        LEFT JOIN favorite_product f ON p.id = f.product_id
                        LEFT JOIN category c ON p.category_id = c.id
                        where p.seller_id != %s AND p.product_state = 1
                        group by p.id
                        order by p.id DESC
                        limit ''' + offset + ''' , ''' + limit + ''';
                        '''
            
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
        
        # 날짜 포맷 변경 
        i = 0
        i = 0
        for i, row in enumerate(result_list):
            result_list[i]['created_at'] = row['created_at'].isoformat()
            result_list[i]['updated_at'] = row['updated_at'].isoformat()
        return {"result" : "success", "items" : result_list, "count" : len(result_list)}, 200
    
class PostDetailResource(Resource) :

    @jwt_required()
    def get(self, id):


        try :
            connection = get_connection()

            query = '''select p.id,seller_id,u.nickname,u.profile_img,category_id,c.name,title,price,description,product_state,max(viewCnt) as viewCnt,i.product_image_url,p.created_at,p.updated_at
                        from products p
                        left join product_image i ON p.id = i.product_id
                        left join users u ON p.seller_id = u.id
                        left join category c on p.category_id = c.id
                        where p.id = %s;
                        '''
            
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
        
        # 날짜 포맷 변경 
        i = 0
        for i, row in enumerate(result_list):
            result_list[i]['created_at'] = row['created_at'].isoformat()
            result_list[i]['updated_at'] = row['updated_at'].isoformat()
        return {"result" : "success", "items" : result_list, "count" : len(result_list)}, 200
    

class PostAddResource(Resource) :

    # 포스팅
    @jwt_required()
    def post(self) :

        seller_id = get_jwt_identity()
        category_id = request.json.get('category_id')
        title = request.json.get('title')
        price = request.json.get('price')
        description = request.json.get('description')
        product_state = 1

    
        try :
            connection = get_connection()

            query = '''insert into products (seller_id, category_id, title, price, description, product_state) 
                    values (%s,%s,%s,%s,%s,%s);'''
            
        
            
            record = (seller_id, category_id, title, price, description,product_state)
            
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)
            id = cursor.lastrowid
            connection.commit()
            


            cursor.close()
            connection.close()


        except Error as e:
            print(Error)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500 
        
        
        return{'result' : 'success',
               'id': id,
               'seller_id' : seller_id,
               'category_id' : category_id,
               'title' : title,
               'price' : price,
               'description' : description,
               'product_state' : product_state},200
    
class PostImageViewResource(Resource) :

    @jwt_required()
    def get(self, product_id) :

    
        try :
            connection = get_connection()
            
            query = '''SELECT id, product_id, product_image_url FROM product_image
                        WHERE product_id = %s;'''
            
            record = (product_id, )
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
        
        return{
            "result" : "success", "items" : result_list, "count" : len(result_list)},200
    
class PostFavoriteListResource(Resource):

    @jwt_required()
    def get(self) :
        id = get_jwt_identity()
        user_id = get_jwt_identity()
        offset = request.args.get('offset')
        limit = request.args.get('limit')

        try :
            connection = get_connection()

            query = '''select p.id,f.user_id,seller_id,category_id,title,c.name,price,description,product_state,i.product_image_url,p.created_at,p.updated_at,min(i.id) as img_id,f.Is_valid
                            from products p
                            LEFT JOIN 
                                (SELECT product_id, MIN(id) as min_img_id
                                FROM product_image
                                GROUP BY product_id) img_min ON p.id = img_min.product_id
                            LEFT JOIN product_image i ON i.id = img_min.min_img_id
                            LEFT JOIN users u ON p.seller_id = u.id
                            LEFT JOIN favorite_product f ON p.id = f.product_id
                            LEFT JOIN category c ON p.category_id = c.id
                            where u.id != %s and f.user_id = %s and p.product_state = 1
                            group by p.id 
                            order by p.id DESC
                            limit ''' + offset + ''' , ''' + limit + ''';
                        '''
            
            record = (id,user_id, )
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
        
        # 날짜 포맷 변경 
        i = 0
        i = 0
        for i, row in enumerate(result_list):
            result_list[i]['created_at'] = row['created_at'].isoformat()
            result_list[i]['updated_at'] = row['updated_at'].isoformat()
        return {"result" : "success", "items" : result_list, "count" : len(result_list)}, 200
    

# 게시글 조회수

class PostViewCountResource(Resource):

    @jwt_required()
    def get(self, id):

        try:
            connection = get_connection()

            query = '''update products
                        set viewCnt = viewCnt + 1
                        where id = %s'''

            record = (id, )
            cursor = connection.cursor(dictionary=True)
            cursor.execute(query,record)
            connection.commit()


            cursor.close()
            connection.close()

        except Error as e:
            print(Error)
            cursor.close()
            connection.close()
            return{"error" : str(e)},500 
        
       
        return {"result" : "success"}, 200
    

class MyPostListResource(Resource):
    
    @jwt_required()
    def get(self) :
        id = get_jwt_identity()
        offset = request.args.get('offset')
        limit = request.args.get('limit')

        try :
            connection = get_connection()

            query = '''select p.id,seller_id,category_id,title,price,description,product_state,c.name,i.product_image_url,p.created_at,p.updated_at,min(i.id) as img_id
                        from products p
                        LEFT JOIN 
                            (SELECT product_id, MIN(id) as min_img_id
                            FROM product_image
                            GROUP BY product_id) img_min ON p.id = img_min.product_id
                        LEFT JOIN product_image i ON i.id = img_min.min_img_id
                        LEFT JOIN users u ON p.seller_id = u.id
                        LEFT JOIN favorite_product f ON p.id = f.product_id
                        LEFT JOIN category c ON p.category_id = c.id
                        where p.seller_id = %s 
                        group by p.id
                        order by p.id DESC
                        limit ''' + offset + ''' , ''' + limit + ''';
                        '''
            
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
        
        # 날짜 포맷 변경 
        i = 0
        i = 0
        for i, row in enumerate(result_list):
            result_list[i]['created_at'] = row['created_at'].isoformat()
            result_list[i]['updated_at'] = row['updated_at'].isoformat()
        return {"result" : "success", "items" : result_list, "count" : len(result_list)}, 200

        
# 게시글 삭제
class DelectPostResource(Resource) :

    @jwt_required()
    def delete(self,product_id) :

        try:
            connection = get_connection()
            cursor = connection.cursor(dictionary=True)

            connection.autocommit = False

            # 첫 번째 DELETE 실행
            query1 = '''DELETE FROM favorite_product WHERE product_id = %s;'''
            cursor.execute(query1, (product_id,))

            # 두 번째 DELETE 실행
            query2 = '''DELETE FROM product_image WHERE product_id = %s;'''
            cursor.execute(query2, (product_id,))

            # 세 번째 DELETE 실행
            query3 = '''DELETE FROM products WHERE id = %s;'''
            cursor.execute(query3, (product_id,))

            # 트랜잭션 커밋
            connection.commit()

            return {'result': 'success'}, 200

        except Exception as e:
            # 예외 발생 시 롤백
            connection.rollback()
            return {'error': str(e)}, 500

        finally:
            if connection.is_connected():
                cursor.close()
                connection.close()


class SearchTitleResource(Resource):
    
    @jwt_required()
    def get(self):

        id = get_jwt_identity()
        keyword = request.args.get('keyword')
        offset = request.args.get('offset')
        limit = request.args.get('limit')

        if not keyword:
            return {"error": "검색어를 입력하세요"}, 400

        try:
            connection = get_connection()

            query = '''SELECT p.id, p.seller_id, p.category_id, p.title, p.price, p.description, p.product_state, 
                       c.name AS name, i.product_image_url, p.created_at, p.updated_at, 
                       MIN(i.id) AS img_id
                        FROM products p
                        LEFT JOIN 
                        (SELECT product_id, MIN(id) AS min_img_id
                        FROM product_image
                        GROUP BY product_id) img_min 
                        ON p.id = img_min.product_id
                        LEFT JOIN product_image i 
                        ON i.id = img_min.min_img_id
                        LEFT JOIN category c 
                        ON p.category_id = c.id
                        WHERE p.title LIKE %s
                        AND p.seller_id != %s
                        GROUP BY p.id
                        ORDER BY p.id DESC
                        limit ''' + offset + ''' , ''' + limit + ''';
                       '''
            search_pattern = f'%{keyword}%'
            record = (search_pattern,id, )

            cursor = connection.cursor(dictionary=True)
            cursor.execute(query, record)
            result_list = cursor.fetchall()

            cursor.close()
            connection.close()

        except Error as e:
            return {"error": str(e)}, 500

         # 날짜 포맷 변경 
        i = 0
        i = 0
        for i, row in enumerate(result_list):
            result_list[i]['created_at'] = row['created_at'].isoformat()
            result_list[i]['updated_at'] = row['updated_at'].isoformat()

        return {"result" : "success", "items" : result_list, "count" : len(result_list)}, 200
    
class StatusResource(Resource):


    @jwt_required()
    def put(self,product_id):

        try :
            connection = get_connection()

            product_state = request.json.get('product_state')  

            query = '''update products
                        set product_state = %s 
                        where id = %s;'''
            

            record = (product_state,product_id, )

            cursor = connection.cursor(dictionary=True)
            cursor.execute(query, record)
            connection.commit()

            cursor.close()
            connection.close()

        except Error as e:
            return {"error": str(e)}, 500
        

        return {"result": "success"}, 200




    



        




            









