from awsconfig import awsconfig

class Config :
    HOST = 'database-1.cfsuebajeebd.ap-northeast-2.rds.amazonaws.com'
    DATABASE = 'Carrot'
    DB_USER = awsconfig.DB_USER
    DB_PASSWORD = awsconfig.DB_PASSWORD

    PASSWORD_SALT = awsconfig.PASSWORD_SALT

    ## 버킷
    S3_BUCKET = 'carrot-server-s3'
    S3_LOCATION =

    AWS_ACCESS_KEY_ID = awsconfig.ACCESS_KEY
    AWS_SECRET_ACCESS_KEY = awsconfig.SECRET_ACCESS_KEY
    

    ### JWT 관련 변수 셋팅
    JWT_SECRET_KEY = awsconfig.JWT_SECRET_KEY
    JWT_ACCESS_TOKEN_EXPIRES = False
    PROPAGATE_EXCEPTIONS = True