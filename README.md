# osm-proto-parser
This library was created basing on existing wiki documentation:

https://wiki.openstreetmap.org/wiki/PBF_Format

and also on already existing library to read PBF files:

https://github.com/openstreetmap/OSM-binary

It was created, because existing solution was not updated for a very long time, which makes it no longer reliable 
and it's build is not working out of the box.

This project goal was to created library that is easy to build and also provides more useful functions.

It is provided under MIT license. 

# Functionality
Library allows user to read OSM in PBF format. They can be found here:

https://planet.openstreetmap.org/pbf/

Current implementation allows to go through whole file or find specific block at given posotion in file.

# Build
Building library requires you to clone this repository and run following command:
> ./gradlew build

Builded library will be in *./build/libs/* directory.

# Usage example

There are two functions that can be used in library at this moment:

- find specific file block at given position,
- analyze whole file given.

To start using this library firstly you have to created library object instance:
> OsmProtoParser osmProtoParser = new OsmProtoParser("map.osm.pbf");

Library constructor accepts file name as only parameter. This parameter should be path to existing OSM file in PBF format. **If file does not exist then *FileNotFoundException* will be thrown.**

### Going through whole file

Reading whole file is implemented using *java.util.concurrent.Flow* API. It's done this way so that very large files 
can be processed by user without very long time of waiting before they are loaded and also to avoid running out of memory.

To start processing whole file user have to call *subscribe* method providing *Flow.Subscriber* implementation.
This requires user to implement *onNext* method, which will be receiving found blocks in file.

    osmProtoParser.subscribe(new Flow.Subscriber<FileBlock>() { 
                 @Override
                 public void onSubscribe(Flow.Subscription subscription) {}
 
                 @Override
                 public void onNext(FileBlock fileBlock) {
                     try {
                         if (fileBlock.getType().equals("OSMHeader")) {
                             Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock
                                     .parseFrom(fileBlock.readBlockData());
 
                         } else if (fileBlock.getType().equals("OSMData")) {
                             Osmformat.PrimitiveBlock primitiveBlock = Osmformat.PrimitiveBlock
                                     .parseFrom(fileBlock.readBlockData());
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
 
                 @Override
                 public void onError(Throwable throwable) {}
 
                 @Override
                 public void onComplete() {
                     System.out.println("Completed!");
                 }
             });

Except *onNext* method there are three more functions that can be implemented here:
- onSubscribe,
- onError,
- onComplete.

Right now only *onComplete* is called by library at the end of processing PBF file. Two other methods currently are not used.

### Finding block at given position
To find block at given position in file you just simply have to call function:
> osmProtoParser.getFileBlockAtPosition(0);

 This function accepts only one parameter, which is position in file where block header definition starts.
 
 As a result you will get *FileBlock* object if position was correct, otherwise function will return *null*.
