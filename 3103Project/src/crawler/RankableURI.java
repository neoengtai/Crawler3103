package crawler;

public class RankableURI implements Comparable<RankableURI>{
	private int rank;
	private String uri;
	
	public RankableURI (int rank, String uri){
		this.rank = rank;
		this.uri = uri;
	}
	
	public String getURI(){
		return this.uri;
	}
	public int getRank(){
		return this.rank;
	}
	
	@Override
	public int compareTo(RankableURI other) {
		if (other.rank > this.rank){
			return 1;
		} else if (other.rank < this.rank){
			return -1;
		}
		return 0;
	}

}
