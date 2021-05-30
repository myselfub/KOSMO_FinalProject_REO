import pymongo
from rest_framework import status
from django.http.response import HttpResponse
from django.views.decorators.csrf import csrf_exempt
import json
from json.encoder import JSONEncoder
from bson import ObjectId
from konlpy.tag import Okt
from collections import Counter
from wordcloud import WordCloud
from django.shortcuts import render
from datetime import datetime


class JSONEncoder(json.JSONEncoder):

    def default(self, o):
        if isinstance(o, ObjectId):
            return str(o)
        return json.JSONEncoder.default(self, o)


mognod = pymongo.MongoClient("mongodb://****:****@localhost:27017/reo?authMechanism=SCRAM-SHA-256")
db = mognod["reo"]


@csrf_exempt
def searchTitleCountList(request):
    if request.method == "GET":
        return HttpResponse(status=status.HTTP_404_NOT_FOUND)
    data = db["searchTitleCount"].find().sort("count", -1)
    data_list = list(data)
    res = json.dumps(data_list, cls=JSONEncoder, ensure_ascii=False)

    return HttpResponse(res, status=status.HTTP_200_OK, content_type=u"application/json; charset=utf-8")


@csrf_exempt
def insertSearchTitleCountList(request, title, mem_email):
    data = {"title": title, "mem_email": mem_email, "date": datetime.now()}
    db["searchTitle"].insert_one(data)

    noun = Okt().nouns(title)
    count = Counter(noun)
    noun_list = count.most_common(100)
    for list_data in noun_list:
        myQuery = {"title": list_data[0]}
        newValues = {"$inc": {"count": list_data[1]}}
        db["searchTitleCount"].update(myQuery, newValues, upsert=True)

    return HttpResponse(status=status.HTTP_200_OK)


@csrf_exempt
def getWordCloud(request):
    if request.method == "GET":
        return HttpResponse(status=status.HTTP_404_NOT_FOUND)
    data = db["searchTitleCount"].find({}, {"_id": 0}).sort("count", -1).limit(100)
    data_list = list(data)
    data_dict = {}
    for i in data_list:
        data_dict[i["title"]] = i["count"]
    font_path = "C:/Windows/Fonts/malgun.ttf"
    wc = WordCloud(font_path, background_color="#F8F8F8", width=400, height=266, max_words=100, max_font_size=100)
    wc.generate_from_frequencies(data_dict)
    wc.to_file("reoPy/static/wordcloud.png")

    return render(request, 'reoPy/wordcloud.html')
