package io.konig.content;

public interface ContentSystem {
	
	CheckBundleResponse checkBundle(AssetBundle bundle) throws ContentAccessException;
	
	int saveMetadata(AssetMetadata metadata) throws ContentAccessException;
	
	AssetMetadata getMetadata(String path) throws ContentAccessException;
	
	int saveAsset(Asset asset) throws ContentAccessException;
	
	Asset getAsset(String path) throws ContentAccessException;

}
