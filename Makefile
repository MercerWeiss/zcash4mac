.PHONY: default
default: macapp ;

APPNAME=zclassic4mac
APPBUNDLE=build/osxapp/zclasic4mac.app
APPBUNDLECONTENTS=$(APPBUNDLE)/Contents
APPBUNDLEEXE=$(APPBUNDLECONTENTS)/MacOS
APPBUNDLERESOURCES=$(APPBUNDLECONTENTS)/Resources
APPBUNDLEICON=$(APPBUNDLECONTENTS)/Resources
BUILD ?= $(shell git rev-list HEAD | wc -l|tr -d [:space:])
SHORTVERSION = 1.0.4a
VERSION ?= $(SHORTVERSION)-$(BUILD)
appbundle: zcash-bin
	sed -i '.bak' 's/@version@/'"$(VERSION)"'/g' src/build/build.xml
	sed -i '.bak' 's/@shortversion@/'"$(SHORTVERSION)"'/; s/@build@/'"$(BUILD)"'/' package/macosx/Info.plist
	ant -f src/build/build.xml osxbundle
	mv src/build/build.xml.bak src/build/build.xml
	mv package/macosx/Info.plist.bak package/macosx/Info.plist
#	mkdir -p $(APPBUNDLE)/Contents/Frameworks
#	cp macosx/first-run.sh $(APPBUNDLEEXE)/
#	cp macosx/logging.properties $(APPBUNDLEEXE)/
#	rm $(APPBUNDLECONTENTS)/PlugIns/jdk1.8.0_77.jdk/Contents/Home/jre/lib/libjfxmedia_qtkit.dylib

icons: macosx/$(APPNAME)Icon.png appbundle
	cp macosx/$(APPNAME).icns $(APPBUNDLEICON)/
	sed -i '' 's/GenericApp.icns/zclass4mac.icns/' $(APPBUNDLECONTENTS)/Info.plist
	rm $(APPBUNDLERESOURCES)/GenericApp.icns

zcash-bin:
	cp macosx/zcash/src/zcashd macosx/zcashd
	cp macosx/zcash/src/zcash-cli macosx/zcash-cli
	dylibbundler -of -b -x macosx/zcashd -d macosx/ -p @executable_path/
	dylibbundler -of -b -x macosx/zcash-cli -d macosx/ -p @executable_path/

macapp: appbundle
