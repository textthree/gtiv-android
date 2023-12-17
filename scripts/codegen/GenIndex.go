package main

import (
	"os"
	"fmt"
	"path/filepath"
	"io/fs"
	"strings"
)

var baseUrl = "Config.Goframe"
var indexFilePath = "./client/src/main/kotlin/org/openapitools/client/apis/" // openapi generate 生成的索引文件目录，相对于当前程序路径
var genFilePath = "/app/src/main/java/com/dqd2022/api/"  // 生成文件存储目录，相对于工程根路径

func main() {
	content := "package com.dqd2022.api\n\n"
	content += "import com.dqd2022.Config\n"
	files := scan()

	// import
	for _, fileName := range files{
		content += "import org.openapitools.client.apis." + fileName + "\n\n"
	}

	content += "class API {\n"
	content += "\tval baseUrl = " + baseUrl + "\n"

	// 属性
	for _, fileName := range files{
		content += "\n"
		content += "\t" + `val ` + fileName[:len(fileName)-3] + ` = ` + fileName + `()`
	}
	content += "\n\n"
	// 方法
	for _, fileName := range files {
		content += "\t" + `private fun ` + fileName + "(): " + fileName + "{\n"
		content += "\t\t" + "val retrofit = RetrofitClient(baseUrl).builder()\n"
		content += "\t\t" + "val service: "+fileName+" = retrofit.create("+fileName+"::class.java)\n"
		content += "\t\t" + "return service"
		content += "\n\t}\n\n"
	}
	content += "}"
	currentDir, _ := os.Getwd()
	dir := filepath.Join( currentDir,"../../")
	fmt.Println(dir + genFilePath + "API.kt")
	createFile(dir + genFilePath + "API.kt", content)
}

// 扫描目录
func scan() []string {
	var files []string
	filepath.Walk(indexFilePath, func(path string, info fs.FileInfo, err error) error {
		if info == nil  {
			return err
		}
		pathArr := strings.Split(path, "/")
		if pathArr[0] == "." || pathArr[len(pathArr)-1] == ".DS_Store" || path == indexFilePath  {
			return nil
		}
		fileName := strings.Split(pathArr[len(pathArr)-1], ".kt")[0]
		files = append(files, fileName)
		return nil
	})
	return files
}

// 创建文件，覆盖创建
func createFile(filepath string, content string) {
	f, err := os.Create(filepath)
	if err != nil {
		fmt.Println(err.Error())
	} else {
		_, err = f.Write([]byte(content))
	}
}
